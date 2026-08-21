package com.example.Service;

import java.util.List;

import com.example.Game.Question;
import com.example.Game.Questions;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Runs one round of the game: the question order, the cursor into it, and the score.
 *
 * <p>One instance per HTTP session. As a singleton it held one player's cursor and score for the
 * whole application, so two people playing at once answered each other's questions.</p>
 *
 * @author Laxmi Poudel
 */
@Service
@SessionScope
public class GameService {

    /** Points for a correct answer. A wrong answer costs nothing. */
    private static final int POINTS_PER_CORRECT_ANSWER = 10;

    private final Questions questions;

    private List<Question> round;
    private Question currentQuestion;
    private int currentQuestionIndex;
    private int score;
    private int right;
    private int wrong;
    private boolean recorded;

    public GameService(Questions questions) {
        this.questions = questions;
        startGame();
    }

    /** Reloads the questions and resets the cursor and the counters. */
    public void startGame() {
        this.round = questions.getQuestion();
        this.currentQuestion = null;
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.right = 0;
        this.wrong = 0;
        this.recorded = false;
    }

    /**
     * @return the next question, or {@code null} once the round is over
     */
    public Question getNextQuestion() {
        if (currentQuestionIndex >= round.size()) {
            return null;
        }
        currentQuestion = round.get(currentQuestionIndex);
        currentQuestionIndex++;
        return currentQuestion;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    /** Scores an answer. Answers are compared ignoring case. */
    public void submitAnswer(Question question, String answer) {
        if (question == null) {
            return;
        }
        if (question.getAnswer().equalsIgnoreCase(answer)) {
            score += POINTS_PER_CORRECT_ANSWER;
            right++;
        } else {
            wrong++;
        }
    }

    public int getScore() {
        return score;
    }

    public int getRight() {
        return right;
    }

    public int getWrong() {
        return wrong;
    }

    /**
     * @return true the first time a finished round is claimed, false afterwards. The end page is
     *         a plain GET, so a refresh would otherwise store the same score again.
     */
    public boolean claimUnrecordedRound() {
        if (recorded) {
            return false;
        }
        recorded = true;
        return true;
    }
}
