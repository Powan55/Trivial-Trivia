package com.example.Persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A database nothing else has touched, so this really is what the migrations left behind.
 *
 * <p>The seed migration and {@code Data/QuestionData.csv} are two copies of the same ten
 * questions. The CSV is what the Pages demo is generated from and the SQL is what the app starts
 * with, so this is the thing that stops them drifting apart.</p>
 */
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:seed-check;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class QuestionSeedTest {

    @Autowired
    private QuestionRepository questions;

    @Test
    void aFreshDatabaseComesUpWithTheShippedQuestionSet() {
        assertThat(questions.count()).isEqualTo(ShippedQuestions.rows().size() - 1);
        assertThat(questions.export()).usingRecursiveComparison().isEqualTo(ShippedQuestions.rows());
    }
}
