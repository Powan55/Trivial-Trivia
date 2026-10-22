package com.example.Persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:seed-check;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class QuestionSeedTest {

    @Autowired
    private QuestionRepository questions;

    @Test
    void aFreshDatabaseComesUpWithTheShippedQuestionSet() {
        assertThat(questions.count()).isEqualTo(ShippedQuestions.rows().size() - 1);
        assertThat(questions.export().stream().map(java.util.Arrays::asList).toList())
                .containsExactlyInAnyOrderElementsOf(ShippedQuestions.rows().stream()
                        .map(java.util.Arrays::asList).toList());
    }
}
