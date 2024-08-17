package com.example.Service;

import com.example.Authentication.User;
import com.example.Game.Question;
import com.example.Game.Questions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing the game logic.
 * Handles the flow of questions, scoring, and game state.
 * @author Laxmi Poudel
 */
@Service
public class GameService {

    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;

    private final Questions queObj;

    /**
     * Constructor for GameService.
     *
     * @param queObj The Questions object responsible for retrieving the questions from the CSV file.
     */
    @Autowired
    public GameService(Questions queObj) {
        this.queObj = queObj;
        this.questions = loadQuestions();
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    /**
     * Loads questions from the Questions object.
     *
     * @return List of Question objects.
     */
    private List<Question> loadQuestions() {
        return queObj.getQuestion();
    }

    /**
     * Starts the game for the given user.
     *
     * @param user The user who is starting the game.
     */
    public void startGame(User user) {
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    /**
     * Retrieves the next question in the sequence.
     *
     * @return The next Question object, or null if there are no more questions.
     */
    public Question getNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex++);
        }
        return null;
    }

    /**
     * Submits the user's answer and updates the score if the answer is correct.
     *
     * @param user     The user submitting the answer.
     * @param question The current question being answered.
     * @param answer   The user's answer.
     */
    public void submitAnswer(User user, Question question, String answer) {
        if (question.getAnswer().equalsIgnoreCase(answer)) {

            score++;
        }
    }

    /**
     * Retrieves the current score for the given user.
     *
     * @param user The user whose score is being retrieved.
     * @return The current score.
     */
    public int getScore(User user) {
        return score;
    }

    /**
     * Ends the game for the given user.
     * Can be used to perform any cleanup or final actions after the game ends.
     *
     * @param user The user ending the game.
     */
    public void endGame(User user) {
        // Perform any cleanup or final actions
    }
}
