package com.example.Game;

import com.example.Database.CSVAdapter;
import com.example.Database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * The {@code Questions} class is responsible for managing and retrieving trivia questions
 * from a CSV file. It uses a {@link Database} adapter to read the file and convert the data
 * into a list of {@link Question} objects.
 *
 * <p>
 * The CSV file should follow a predefined structure, where each row represents a question
 * and its associated options and answer.
 * </p>
 *
 * <p>
 * Example CSV structure:
 * <pre>
 * ID, Question, Option1, Option2, Option3, Option4, Answer
 * 1, What is the capital of France?, Paris, Lyon, Marseille, Nice, Paris
 * </pre>
 * </p>
 *
 * @author Laxmi Poudel
 */
@Component
public class Questions {

    private ArrayList<String[]> question;
    private final String fileName;

    @Autowired
    private final Database database;

    /**
     * Constructs a new {@code Questions} object and initializes the database adapter.
     * The CSV file path is also specified during construction.
     */
    public Questions() {
        database = new CSVAdapter();
        question = new ArrayList<>();
        fileName = "Data/QuestionData.csv";
    }

    /**
     * Reads the questions from the CSV file and returns them as a list of {@link Question} objects.
     *
     * @return A list of {@link Question} objects containing the data from the CSV file.
     */
    public ArrayList<Question> getQuestion() {
        ArrayList<Question> questions = new ArrayList<>();
        question = database.readFile(fileName);
        for (String[] data : question.subList(1, question.size())) {
            questions.add(new Question(data[0], data[1], data[2], data[3], data[4], data[5]));
        }
        return questions;
    }
}
