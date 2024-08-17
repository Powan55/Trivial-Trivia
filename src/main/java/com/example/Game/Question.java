package com.example.Game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Represents a single trivia question with four possible options and an answer.
 * Each question has a unique ID, the question text, four answer options, and the correct answer.
 *
 * <p>
 * This class is used in the trivia game to present questions to users and validate their answers.
 * </p>
 *
 * @author Laxmi Poudel
 */
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String answer;
    private String option1, option2, option3, option4;

    // Constructor
    public Question(String question, String option1, String option2, String option3, String option4, String answer) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
    }

    // No-arg constructor for JPA
    protected Question() {}

    // Getter methods

    /**
     * Gets the unique identifier for the question.
     *
     * @return The question ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the text of the question.
     *
     * @return The question text.
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Gets the correct answer for the question.
     *
     * @return The correct answer.
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Gets the first answer option.
     *
     * @return The first answer option.
     */
    public String getOption1() {
        return option1;
    }

    /**
     * Gets the second answer option.
     *
     * @return The second answer option.
     */
    public String getOption2() {
        return option2;
    }

    /**
     * Gets the third answer option.
     *
     * @return The third answer option.
     */
    public String getOption3() {
        return option3;
    }

    /**
     * Gets the fourth answer option.
     *
     * @return The fourth answer option.
     */
    public String getOption4() {
        return option4;
    }

    /**
     * Gets the answer option by its index.
     *
     * @param index The index of the answer option (1-4).
     * @return The answer option at the specified index, or an empty string if the index is invalid.
     */
    public String getOption(int index) {
        switch (index) {
            case 1:
                return option1;
            case 2:
                return option2;
            case 3:
                return option3;
            case 4:
                return option4;
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s\n1) %-15s 2) %-15s\n3) %-15s 4) %-15s",
                question, option1, option2, option3, option4);
    }
}
