package com.example.Service;

import java.util.ArrayList;
import java.util.List;

import com.example.Game.Question;
import com.example.Persistence.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameServiceTest {

    private static final Question CAPITAL =
            new Question("Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris");
    private static final Question PLANET =
            new Question("Red planet?", "Venus", "Jupiter", "Mars", "Saturn", "Mars");

    private GameService game;

    @BeforeEach
    void setUp() {
        QuestionRepository questions = mock(QuestionRepository.class);
        when(questions.findAll()).thenAnswer(invocation -> new ArrayList<>(List.of(CAPITAL, PLANET)));
        game = new GameService(questions);
    }

    @Test
    void startsAtZero() {
        assertThat(game.getScore()).isZero();
        assertThat(game.getRight()).isZero();
        assertThat(game.getWrong()).isZero();
        assertThat(game.getCurrentQuestion()).isNull();
    }

    @Test
    void walksTheQuestionsInOrderThenReturnsNull() {
        assertThat(game.getNextQuestion()).isSameAs(CAPITAL);
        assertThat(game.getNextQuestion()).isSameAs(PLANET);
        assertThat(game.getNextQuestion()).isNull();
    }

    @Test
    void currentQuestionTracksWhatWasHandedOut() {
        game.getNextQuestion();
        assertThat(game.getCurrentQuestion()).isSameAs(CAPITAL);
    }

    @Test
    void theQuestionNumberIsOneBasedAndTracksTheQuestionOnScreen() {
        assertThat(game.getQuestionNumber()).isZero();
        game.getNextQuestion();
        assertThat(game.getQuestionNumber()).isEqualTo(1);
        game.getNextQuestion();
        assertThat(game.getQuestionNumber()).isEqualTo(2);
    }

    @Test
    void theLastQuestionNumberMatchesTheTotal() {
        assertThat(game.getTotalQuestions()).isEqualTo(2);
        game.getNextQuestion();
        game.getNextQuestion();
        assertThat(game.getNextQuestion()).isNull();
        assertThat(game.getQuestionNumber()).isEqualTo(game.getTotalQuestions());
    }

    @Test
    void submitAnswerReportsWhetherItWasRight() {
        assertThat(game.submitAnswer(CAPITAL, "Paris")).isTrue();
        assertThat(game.submitAnswer(CAPITAL, "London")).isFalse();
        assertThat(game.submitAnswer(null, "Paris")).isFalse();
    }

    @Test
    void aCorrectAnswerScoresTen() {
        game.submitAnswer(CAPITAL, "Paris");
        assertThat(game.getScore()).isEqualTo(10);
        assertThat(game.getRight()).isEqualTo(1);
        assertThat(game.getWrong()).isZero();
    }

    @Test
    void answersAreComparedIgnoringCase() {
        game.submitAnswer(CAPITAL, "pArIs");
        assertThat(game.getScore()).isEqualTo(10);
    }

    @Test
    void aWrongAnswerCostsNothingAndStillCounts() {
        game.submitAnswer(CAPITAL, "London");
        game.submitAnswer(PLANET, "Venus");
        assertThat(game.getScore()).isZero();
        assertThat(game.getWrong()).isEqualTo(2);
        assertThat(game.getRight()).isZero();
    }

    @Test
    void wrongAnswersKeepCountingOnceTheScoreIsZero() {
        game.submitAnswer(CAPITAL, "Paris");
        game.submitAnswer(PLANET, "Venus");
        game.submitAnswer(PLANET, "Jupiter");
        game.submitAnswer(PLANET, "Saturn");
        assertThat(game.getScore()).isEqualTo(10);
        assertThat(game.getWrong()).isEqualTo(3);
    }

    @Test
    void aNullQuestionIsIgnoredRatherThanThrowing() {
        game.submitAnswer(null, "Paris");
        assertThat(game.getScore()).isZero();
        assertThat(game.getWrong()).isZero();
    }

    @Test
    void startGameResetsTheCursorAndTheCounters() {
        game.getNextQuestion();
        game.submitAnswer(CAPITAL, "Paris");

        game.startGame();

        assertThat(game.getScore()).isZero();
        assertThat(game.getRight()).isZero();
        assertThat(game.getWrong()).isZero();
        assertThat(game.getNextQuestion()).isSameAs(CAPITAL);
        assertThat(game.getQuestionNumber()).isEqualTo(1);
    }

    @Test
    void anEmptyQuestionSetEndsImmediately() {
        QuestionRepository none = mock(QuestionRepository.class);
        when(none.findAll()).thenReturn(new ArrayList<>());
        assertThat(new GameService(none).getNextQuestion()).isNull();
    }
}
