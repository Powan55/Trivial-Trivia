package com.example.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.example.Persistence.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RoundServiceTest {
    @Autowired RoundService rounds;
    @Autowired QuestionRepository questions;
    @Autowired JdbcClient jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void refreshDuplicatesOwnershipAndReload() {
        int id = rounds.start("owner", null);
        var page = rounds.serve(id, "owner", null);
        var servedAt = jdbc.sql("select served_at from round_questions where round_id = ? and position = 1")
                .param(id).query(java.sql.Timestamp.class).single();
        assertThat(rounds.serve(id, "owner", null).position()).isEqualTo(1);
        assertThat(jdbc.sql("select served_at from round_questions where round_id = ? and position = 1")
                .param(id).query(java.sql.Timestamp.class).single()).isEqualTo(servedAt);
        assertThatThrownBy(() -> rounds.serve(id, "other", null)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> rounds.serve(id, "owner", "login")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> rounds.answer(id, 2, "Paris", "owner", null)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> rounds.answer(id, 1, "forged", "owner", null)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        var verdict = rounds.answer(id, 1, page.question().getAnswer(), "owner", null);
        assertThat(rounds.answer(id, 1, page.question().getOption1(), "owner", null)).isEqualTo(verdict);
        assertThat(new RoundService(jdbc).summary(id, "owner", null).score()).isEqualTo(10);
        assertThat(rounds.serve(id, "owner", null).position()).isEqualTo(2);
    }

    @Test
    void rollbackRestoresQuestionAndTotals() {
        int id = rounds.start("rollback", null);
        var page = rounds.serve(id, "rollback", null);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            rounds.answer(id, 1, page.question().getAnswer(), "rollback", null);
            status.setRollbackOnly();
        });
        assertThat(rounds.summary(id, "rollback", null).score()).isZero();
        assertThat(rounds.serve(id, "rollback", null).position()).isEqualTo(1);
    }

    @Test
    void importKeepsSnapshotsAndEmptyBankCannotFinishRound() {
        var original = questions.export();
        int id = rounds.start("import", null);
        var snapshot = rounds.serve(id, "import", null);
        rounds.answer(id, 1, snapshot.question().getAnswer(), "import", null);
        try {
            questions.replaceAll(List.<String[]>of(QuestionRepository.HEADER));
            assertThat(jdbc.sql("select answer from round_questions where round_id = ? and position = 1")
                    .param(id).query(String.class).single()).isEqualTo(snapshot.question().getAnswer());
            assertThat(rounds.summary(id, "import", null).score()).isEqualTo(10);
            int before = jdbc.sql("select count(*) from rounds").query(Integer.class).single();
            assertThatThrownBy(() -> rounds.start("empty", null)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            assertThat(jdbc.sql("select count(*) from rounds").query(Integer.class).single()).isEqualTo(before);
        } finally {
            questions.replaceAll(original);
        }
        int next = rounds.start("new", null);
        assertThat(rounds.summary(next, "new", null).total()).isEqualTo(original.size() - 1);
        assertThat(rounds.serve(next, "new", null).question().getQuestion()).isEqualTo(original.get(1)[0]);
    }

    @Test
    void concurrentFinalSubmissionsFinishExactlyOnce() throws Exception {
        int id = rounds.start("race", null);
        var page = rounds.serve(id, "race", null);
        while (page.position() < page.total()) {
            rounds.answer(id, page.position(), page.question().getAnswer(), "race", null);
            page = rounds.serve(id, "race", null);
        }
        final var last = page;
        String wrong = java.util.stream.IntStream.rangeClosed(1, 4).mapToObj(last.question()::getOption)
                .filter(option -> !option.equals(last.question().getAnswer())).findFirst().orElseThrow();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> { ready.countDown(); start.await(); return rounds.answer(id, last.position(), last.question().getAnswer(), "race", null); });
            var b = pool.submit(() -> { ready.countDown(); start.await(); return rounds.answer(id, last.position(), wrong, "race", null); });
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(a.get(10, TimeUnit.SECONDS)).isEqualTo(b.get(10, TimeUnit.SECONDS));
            var result = rounds.summary(id, "race", null);
            assertThat(result.right() + result.wrong()).isEqualTo(last.total());
            assertThat(result.score()).isEqualTo(result.right() * 10);
            assertThat(result.finished()).isTrue();
            assertThat(rounds.serve(id, "race", null)).isNull();
        } finally { start.countDown(); pool.shutdownNow(); }
    }
    @Test
    void completionStoresSignedInScoreWithoutEndVisitAndKeepsSessionsSeparate() {
        String username = "durable_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        new com.example.Persistence.UserRepository(jdbc).insert("Player", username, "unused_hash");
        int id = rounds.start("signed", username);
        int other = rounds.start("separate", null);
        var history = new com.example.Persistence.RoundRepository(jdbc);
        assertThat(history.scoresFor(username)).isEmpty();
        while (!rounds.summary(id, "signed", username).finished()) {
            var page = rounds.serve(id, "signed", username);
            rounds.answer(id, page.position(), page.question().getAnswer(), "signed", username);
        }
        assertThat(history.scoresFor(username)).containsExactly(rounds.summary(id, "signed", username).total() * 10);
        assertThat(rounds.summary(other, "separate", null).score()).isZero();
        assertThat(rounds.summary(other, "separate", null).finished()).isFalse();
        assertThatThrownBy(() -> rounds.answer(id, 1, "Paris", "signed", null))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> rounds.summary(Integer.MAX_VALUE, "signed", username))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void databaseFailureRollsBackAnswerAlongWithAggregate() {
        int id = rounds.start("failure", null);
        var page = rounds.serve(id, "failure", null);
        while (page.position() < page.total()) {
            rounds.answer(id, page.position(), page.question().getAnswer(), "failure", null);
            page = rounds.serve(id, "failure", null);
        }
        final var last = page;
        final int before = rounds.summary(id, "failure", null).score();
        jdbc.sql("alter table rounds add constraint test_round_failure check (id <> " + id + " or score = " + before + ")").update();
        try {
            assertThatThrownBy(() -> rounds.answer(id, last.position(), last.question().getAnswer(), "failure", null))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
            assertThat(rounds.summary(id, "failure", null).score()).isEqualTo(before);
            assertThat(rounds.serve(id, "failure", null).position()).isEqualTo(last.position());
            assertThat(jdbc.sql("""
                    select count(*) from round_questions where round_id = ? and position = ?
                    and submitted_answer is null and correct is null and answered_at is null
                    """
                    ).params(id, last.position()).query(Integer.class).single()).isEqualTo(1);
        } finally {
            jdbc.sql("alter table rounds drop constraint test_round_failure").update();
        }
        rounds.answer(id, last.position(), last.question().getAnswer(), "failure", null);
        assertThat(rounds.summary(id, "failure", null).score()).isEqualTo(before + 10);
    }
}
