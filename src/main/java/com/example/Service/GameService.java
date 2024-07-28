package com.example.Service;


import com.example.Authentication.User;
import com.example.Game.Question;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;

    public GameService() {
        this.questions = loadQuestions();
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    private List<Question> loadQuestions() {
        // Ideally, questions should be loaded from a database or a file.
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(1L, "What is the capital of France?", "Berlin", "Madrid", "Paris", "Rome", "Paris"));
        questions.add(new Question(2L, "What is 2 + 2?", "3", "4", "5", "6", "4"));
        // Add more questions as needed
        return questions;
    }

    public void startGame(User user) {
        this.currentQuestionIndex = 0;
        this.score = 0;
    }

    public Question getNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex++);
        }
        return null;
    }

    public void submitAnswer(User user, Question question, String answer) {
        if (question.getAnswer().equalsIgnoreCase(answer)) {
            score++;
        }
    }

    public int getScore(User user) {
        return score;
    }

    public void endGame(User user) {
        // Perform any cleanup or final actions
    }
}