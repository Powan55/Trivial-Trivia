package com.example.Persistence;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RoundRepositoryTest {

    @Autowired
    private RoundRepository rounds;

    @Autowired
    private UserRepository users;

    @BeforeEach
    void givenAPlayer() {
        users.insert("Ada Lovelace", "round_ada", "hash");
    }

    @Test
    void storesAFinishedRoundAndReadsItBack() {
        assertThat(rounds.record("round_ada", 40, 4, 6)).isTrue();
        assertThat(rounds.scoresFor("round_ada")).contains(40);
    }

    @Test
    void scoresComeBackOldestFirst() {
        users.insert("Order", "round_order", "hash");
        rounds.record("round_order", 10, 1, 9);
        rounds.record("round_order", 90, 9, 1);
        rounds.record("round_order", 50, 5, 5);

        assertThat(rounds.scoresFor("round_order")).containsExactly(10, 90, 50);
    }

    @Test
    void anUnknownPlayerStoresNothing() {
        assertThat(rounds.record("round_nobody", 10, 1, 0)).isFalse();
        assertThat(rounds.scoresFor("round_nobody")).isEmpty();
    }

    @Test
    void aGuestHasNoUsernameAndNoHistory() {
        assertThat(rounds.record(null, 10, 1, 0)).isFalse();
        assertThat(rounds.scoresFor(null)).isEmpty();
    }

    @Test
    void onePlayersRoundsAreNotAnothers() {
        users.insert("Grace Hopper", "round_grace", "hash");
        rounds.record("round_grace", 70, 7, 3);

        assertThat(rounds.scoresFor("round_grace")).containsExactly(70);
        assertThat(rounds.scoresFor("round_ada")).doesNotContain(70);
    }

    /** Rounds finishing at the same moment used to overwrite each other. Every one lands now. */
    @Test
    void simultaneousFinishesAllLand() throws Exception {
        users.insert("Busy", "round_busy", "hash");
        int finishers = 12;
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(finishers);
        try {
            List<Future<Boolean>> futures = IntStream.range(0, finishers)
                    .mapToObj(i -> pool.submit(() -> {
                        go.await(5, TimeUnit.SECONDS);
                        return rounds.record("round_busy", i * 10, i, 10 - i);
                    }))
                    .collect(Collectors.toList());
            go.countDown();
            for (Future<Boolean> future : futures) {
                assertThat(future.get(30, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(rounds.scoresFor("round_busy")).hasSize(finishers);
    }
}
