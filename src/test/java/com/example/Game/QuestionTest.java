package com.example.Game;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionTest {

    private final Question question =
            new Question("Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris");

    @Test
    void exposesTheTextAndTheAnswer() {
        assertThat(question.getQuestion()).isEqualTo("Capital of France?");
        assertThat(question.getAnswer()).isEqualTo("Paris");
    }

    @Test
    void returnsOptionsByOneBasedIndex() {
        assertThat(question.getOption(1)).isEqualTo("London");
        assertThat(question.getOption(2)).isEqualTo("Berlin");
        assertThat(question.getOption(3)).isEqualTo("Madrid");
        assertThat(question.getOption(4)).isEqualTo("Paris");
    }

    @Test
    void anOutOfRangeIndexIsEmptyRatherThanAnException() {
        assertThat(question.getOption(0)).isEmpty();
        assertThat(question.getOption(5)).isEmpty();
        assertThat(question.getOption(-1)).isEmpty();
    }

    @Test
    void toStringNumbersTheOptions() {
        assertThat(question.toString())
                .startsWith("Capital of France?")
                .contains("1) London")
                .contains("4) Paris");
    }
}
