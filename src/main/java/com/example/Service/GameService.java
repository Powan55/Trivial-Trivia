package com.example.Service;

import java.util.List;

import com.example.Game.Question;
import com.example.Game.Questions;
import org.springframework.stereotype.Service;

/**
 * Runs one round of the game: the question order, the cursor into it, and the score.
 *
 * @author Laxmi Poudel
 */
@Service
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
}
