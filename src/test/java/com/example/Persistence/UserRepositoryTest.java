package com.example.Persistence;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void storesAUserAndReadsItBack() {
        assertThat(users.insert("Ada Lovelace", "repo_ada", "$2a$10$hash")).isTrue();

        assertThat(users.findByUsername("repo_ada")).hasValueSatisfying(stored -> {
            assertThat(stored.username()).isEqualTo("repo_ada");
            assertThat(stored.displayName()).isEqualTo("Ada Lovelace");
            assertThat(stored.passwordHash()).isEqualTo("$2a$10$hash");
            assertThat(stored.id()).isPositive();
        });
    }

    @Test
    void anUnknownUsernameIsEmptyRatherThanAnException() {
        assertThat(users.findByUsername("repo_nobody")).isEmpty();
        assertThat(users.findByUsername(null)).isEmpty();
    }

    @Test
    void aTakenUsernameIsRefusedRatherThanThrowing() {
        assertThat(users.insert("First", "repo_taken", "hash-one")).isTrue();
        assertThat(users.insert("Second", "repo_taken", "hash-two")).isFalse();

        assertThat(users.findByUsername("repo_taken"))
                .hasValueSatisfying(stored -> assertThat(stored.displayName()).isEqualTo("First"));
    }

    @Test
    void usernamesAreStillCaseSensitive() {
        assertThat(users.insert("Ada", "repo_Case", "hash")).isTrue();
        assertThat(users.findByUsername("repo_case")).isEmpty();
        assertThat(users.insert("Ada", "repo_case", "hash")).isTrue();
    }

    @Test
    void countsWhatIsStored() {
        long before = users.count();
        users.insert("Counted", "repo_counted", "hash");
        assertThat(users.count()).isEqualTo(before + 1);
    }

    /**
     * The bug this milestone exists to kill. Six people registering the same username at once used
     * to leave whichever write finished last, and told all six they had an account. Exactly one
     * row can exist now, whatever the timing, because the database says so rather than a read the
     * next writer has already invalidated.
     */
    @Test
    void onlyOneOfManySimultaneousRegistrationsOfOneUsernameWins() throws Exception {
        int racers = 12;
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(racers);
        try {
            List<Callable<Boolean>> attempts = IntStream.range(0, racers)
                    .<Callable<Boolean>>mapToObj(i -> () -> {
                        go.await(5, TimeUnit.SECONDS);
                        return users.insert("Racer " + i, "repo_race", "hash-" + i);
                    })
                    .collect(Collectors.toList());

            List<Future<Boolean>> futures = attempts.stream().map(pool::submit).collect(Collectors.toList());
            go.countDown();

            long won = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(30, TimeUnit.SECONDS)) {
                    won++;
                }
            }
            assertThat(won).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }

        assertThat(jdbc.sql("select count(*) from users where username = 'repo_race'")
                .query(Long.class).single()).isEqualTo(1);
    }

    /** Distinct usernames all land, which is the other half of what six parallel signups lost. */
    @Test
    void simultaneousRegistrationsOfDistinctUsernamesAllLand() throws Exception {
        int racers = 12;
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(racers);
        try {
            List<Future<Boolean>> futures = IntStream.range(0, racers)
                    .mapToObj(i -> pool.submit(() -> {
                        go.await(5, TimeUnit.SECONDS);
                        return users.insert("Racer " + i, "repo_parallel_" + i, "hash-" + i);
                    }))
                    .collect(Collectors.toList());
            go.countDown();
            for (Future<Boolean> future : futures) {
                assertThat(future.get(30, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(IntStream.range(0, racers).mapToObj(i -> "repo_parallel_" + i))
                .allSatisfy(username -> assertThat(users.findByUsername(username)).isPresent());
    }
}
