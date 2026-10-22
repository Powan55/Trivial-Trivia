package com.example.Persistence;

import java.util.ArrayList;
import java.util.List;

import com.example.Game.Question;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class QuestionRepositoryTest {

    private static final String[] CAPITAL =
            {"Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris"};
    private static final String[] PLANET =
            {"Red planet?", "Venus", "Jupiter", "Mars", "Saturn", "Mars"};

    @Autowired
    private QuestionRepository questions;

    @BeforeEach
    @AfterEach
    void startFromTheShippedSet() {
        questions.replaceAll(ShippedQuestions.rows());
    }

    private static List<String[]> withHeader(String[]... rows) {
        List<String[]> all = new ArrayList<>();
        all.add(QuestionRepository.LEGACY_HEADER.clone());
        all.addAll(List.of(rows));
        return all;
    }

    private static String[] extended(String[] row) {
        String[] result = java.util.Arrays.copyOf(row, 8);
        result[6] = "Uncategorized";
        result[7] = "MEDIUM";
        return result;
    }

    @Test
    void readsWhatIsStored() {
        assertThat(questions.count()).isEqualTo(10);
        assertThat(questions.findAll().get(0).getQuestion()).isEqualTo("What is the capital of France?");
    }

    @Test
    void replacingTheSetLeavesOnlyTheNewRows() {
        questions.replaceAll(withHeader(CAPITAL, PLANET));

        List<Question> stored = questions.findAll();
        assertThat(stored).hasSize(2);
        assertThat(questions.count()).isEqualTo(2);
        assertThat(stored.get(0).getQuestion()).isEqualTo("Capital of France?");
        assertThat(stored.get(0).getOption(4)).isEqualTo("Paris");
        assertThat(stored.get(1).getAnswer()).isEqualTo("Mars");
    }

    @Test
    void aHeaderOnlyImportEmptiesTheSet() {
        questions.replaceAll(withHeader());
        assertThat(questions.findAll()).isEmpty();
        assertThat(questions.export()).containsExactly(QuestionRepository.HEADER);
    }

    @Test
    void exportPutsTheHeaderBackAndKeepsTheOrder() {
        questions.replaceAll(withHeader(CAPITAL, PLANET));

        assertThat(questions.export()).containsExactly(QuestionRepository.HEADER, extended(CAPITAL), extended(PLANET));
    }

    /** An answer matching none of its options is an unanswerable question. The schema says no. */
    @Test
    void theDatabaseRefusesAnAnswerThatIsNotOneOfTheOptions() {
        assertThatThrownBy(() -> questions.replaceAll(withHeader(
                new String[] {"Capital of France?", "London", "Berlin", "Madrid", "Lyon", "Paris"})))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** A refused row leaves the set it was going to replace exactly as it was. */
    @Test
    void aRefusedImportRollsBack() {
        assertThatThrownBy(() -> questions.replaceAll(withHeader(CAPITAL,
                new String[] {"Red planet?", "Venus", "Jupiter", "Mars", "Saturn", "Pluto"})))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(questions.count()).isEqualTo(10);
    }
}
