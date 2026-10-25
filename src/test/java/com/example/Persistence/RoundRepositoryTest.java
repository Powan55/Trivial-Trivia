package com.example.Persistence;

import java.util.List;
import java.time.LocalDateTime;
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
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RoundRepositoryTest {

    @Autowired
    private RoundRepository rounds;

    @Autowired
    private UserRepository users;

    @Autowired
    private JdbcClient jdbc;

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

    @Test
    void historyUsesCompletionDatesSavedCountsAndHonestLegacyMetadata() {
        String username = "round_details";
        users.insert("History", username, "hash");
        saved(username, 37, 3, 2, 5, "2026-10-20T12:15:30");
        saved(username, 70, 7, 3, 10, "2026-10-19T12:15:30");
        saved(username, 10, 1, 0, 1, "2026-10-21T12:15:30");
        saved(username, 0, 0, 5, 5, "2026-10-22T12:15:30");
        saved(username, 40, 4, 6, 0, "2026-10-22T12:15:30");
        saved(username, 90, 0, 0, 0, "2026-10-30T12:15:30");
        saved(username, 0, 0, 0, 0, "2026-10-31T12:15:30");
        saved(username, 999, 1, 0, 5, null);

        var history = rounds.historyFor(username);
        assertThat(history).extracting(RoundRepository.HistoryEntry::score)
                .containsExactly(40, 0, 10, 37, 70, 0, 90);
        assertThat(history).extracting(RoundRepository.HistoryEntry::correctCount)
                .containsExactly(4, 0, 1, 3, 7, null, null);
        assertThat(history).extracting(RoundRepository.HistoryEntry::totalQuestions)
                .containsExactly(10, 5, 1, 5, 10, null, null);
        assertThat(history.get(0).finishedAt()).isEqualTo(LocalDateTime.parse("2026-10-22T12:15:30"));
        assertThat(history.subList(5, 7)).extracting(RoundRepository.HistoryEntry::finishedAt)
                .containsOnlyNulls();
        assertThat(rounds.scoresFor(username)).containsExactly(37, 70, 10, 0, 40, 90, 0);
        assertThat(rounds.historyFor(null)).isEmpty();
        assertThat(rounds.historyFor("round_details_unknown")).isEmpty();
        users.insert("Other", "round_details_other", "hash");
        saved("round_details_other", 888, 1, 0, 1, "2026-11-01T12:15:30");
        assertThat(rounds.historyFor(username)).isEqualTo(history);
    }

    @Test
    void malformedTalliesKeepTheirDatesAndPointsWithoutInventingAccuracy() {
        String username = "round_malformed";
        users.insert("History", username, "hash");
        saved(username, 1, -1, 6, 5, "2026-10-22T12:15:30");
        saved(username, 2, 3, -1, 0, "2026-10-22T12:15:30");
        saved(username, 3, 3, 3, 5, "2026-10-22T12:15:30");
        saved(username, 4, 1, 0, -1, "2026-10-22T12:15:30");
        saved(username, 5, 0, 0, 5, "2026-10-22T12:15:30");
        saved(username, 6, Integer.MAX_VALUE, 1, 0, "2026-10-22T12:15:30");

        var history = rounds.historyFor(username);
        assertThat(history).extracting(RoundRepository.HistoryEntry::score).containsExactly(6, 5, 4, 3, 2, 1);
        assertThat(history).allSatisfy(entry -> {
            assertThat(entry.finishedAt()).isEqualTo(LocalDateTime.parse("2026-10-22T12:15:30"));
            assertThat(entry.correctCount()).isNull();
            assertThat(entry.totalQuestions()).isNull();
        });
    }

    private void saved(String username, int score, int correct, int wrong, int total, String finishedAt) {
        jdbc.sql("""
                insert into rounds (user_id, score, correct_count, wrong_count, total_questions, finished_at)
                select id, :score, :correct, :wrong, :total, :finished from users where username = :username
                """)
                .param("score", score).param("correct", correct).param("wrong", wrong).param("total", total)
                .param("finished", finishedAt == null ? null : LocalDateTime.parse(finishedAt), java.sql.Types.TIMESTAMP)
                .param("username", username).update();
    }
}
