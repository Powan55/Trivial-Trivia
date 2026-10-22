package com.example.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.example.Persistence.QuestionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CategoryRoundTest {
    @Autowired RoundService rounds;
    @Autowired QuestionRepository questions;
    @Autowired JdbcClient jdbc;
    private List<String[]> original;

    @BeforeEach
    void remember() {
        original = questions.export();
    }

    @AfterEach
    void restore() {
        questions.replaceAll(original);
    }

    private String[] row(int number, String category, String difficulty) {
        return new String[]{"Question " + number, "A", "B", "C", "D", "A", category, difficulty};
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 5, 7})
    void samplesWithoutReplacementAndCapsAtFive(int available) {
        List<String[]> bank = new ArrayList<>();
        bank.add(QuestionRepository.HEADER);
        for (int i = 0; i < available; i++) bank.add(row(i, "Science", "HARD"));
        bank.add(row(100, "History", "EASY"));
        questions.replaceAll(bank);
        var setup = rounds.setup("Science", "HARD");
        assertThat(setup.categories()).containsExactly("History", "Science");
        assertThat(setup.matchingCount()).isEqualTo(available);
        assertThat(setup.roundSize()).isEqualTo(Math.min(5, available));
        int id = rounds.start("sample", null, "Science", "HARD");
        var seen = new HashSet<String>();
        while (!rounds.summary(id, "sample", null).finished()) {
            var page = rounds.serve(id, "sample", null);
            assertThat(seen.add(page.question().getQuestion())).isTrue();
            assertThat(page.question().getQuestion()).isNotEqualTo("Question 100");
            rounds.answer(id, page.position(), page.question().getAnswer(), "sample", null);
        }
        assertThat(seen).hasSize(Math.min(5, available));
        questions.replaceAll(List.<String[]>of(QuestionRepository.HEADER));
        assertThat(jdbc.sql("select selected_category from rounds where id = ?").param(id).query(String.class).single())
                .isEqualTo("Science");
        assertThat(jdbc.sql("select selected_difficulty from rounds where id = ?").param(id).query(String.class).single())
                .isEqualTo("HARD");
        assertThat(rounds.summary(id, "sample", null).total()).isEqualTo(Math.min(5, available));
    }

    @Test
    void startUsesCurrentBankAndKeepsSnapshotsAfterAnotherImport() {
        List<String[]> bank = new ArrayList<>();
        bank.add(QuestionRepository.HEADER);
        for (int i = 0; i < 7; i++) bank.add(row(i, "Science", "HARD"));
        questions.replaceAll(bank);
        assertThat(rounds.setup("Science", "HARD").roundSize()).isEqualTo(5);

        questions.replaceAll(List.of(QuestionRepository.HEADER, row(100, "Science", "HARD")));
        int id = rounds.start("updated-bank", null, "Science", "HARD");
        questions.replaceAll(List.of(QuestionRepository.HEADER, row(200, "History", "EASY")));

        var page = rounds.serve(id, "updated-bank", null);
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.question().getQuestion()).isEqualTo("Question 100");
        rounds.answer(id, page.position(), "A", "updated-bank", null);
        assertThat(rounds.summary(id, "updated-bank", null).finished()).isTrue();
        assertThat(jdbc.sql("select selected_category from rounds where id = ?").param(id).query(String.class).single())
                .isEqualTo("Science");
    }

    @Test
    void filtersIndependentlyAndCombinedAndRejectsStaleOrEmptySelection() {
        questions.replaceAll(List.of(QuestionRepository.HEADER, row(1, "Science", "EASY"),
                row(2, "Science", "HARD"), row(3, "History", "EASY")));
        assertThat(rounds.setup(null, null).matchingCount()).isEqualTo(3);
        assertThat(rounds.setup(" Science ", "").matchingCount()).isEqualTo(2);
        assertThat(rounds.setup("", "EASY").matchingCount()).isEqualTo(2);
        assertThat(rounds.setup("Science", "HARD").matchingCount()).isEqualTo(1);
        assertThat(rounds.setup("History", "HARD").roundSize()).isZero();
        int count = jdbc.sql("select count(*) from rounds").query(Integer.class).single();
        assertThatThrownBy(() -> rounds.start("empty", null, "History", "HARD"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> rounds.start("stale", null, "Sport", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rounds.start("invalid", null, null, "easy")).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.sql("select count(*) from rounds").query(Integer.class).single()).isEqualTo(count);
        jdbc.sql("insert into categories (name) values ('Orphan')").update();
        assertThat(rounds.setup(null, null).categories()).doesNotContain("Orphan");
    }
}
