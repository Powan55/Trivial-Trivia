package com.example.Game;

import java.util.ArrayList;
import java.util.List;

import com.example.Database.DataFiles;
import com.example.Database.Database;
import org.springframework.stereotype.Component;

/**
 * Loads the trivia questions.
 *
 * <p>The file is a CSV with a header row:</p>
 *
 * <pre>
 * Question,Option1,Option2,Option3,Option4,Answer
 * What is the capital of France?,London,Berlin,Madrid,Paris,Paris
 * </pre>
 *
 * @author Laxmi Poudel
 */
@Component
public class Questions {

    /** Header plus one option per column plus the answer. */
    private static final int COLUMNS = 6;

    private final Database database;

    public Questions(Database database) {
        this.database = database;
    }

    /**
     * Reads the questions and returns them, skipping the header row.
     *
     * <p>A missing or empty file yields an empty list. Rows with the wrong number of columns are
     * skipped rather than failing the whole read.</p>
     */
    public ArrayList<Question> getQuestion() {
        ArrayList<Question> questions = new ArrayList<>();
        List<String[]> rows = database.readFile(DataFiles.QUESTIONS);
        if (rows.size() < 2) {
            return questions;
        }
        for (String[] row : rows.subList(1, rows.size())) {
            if (row.length < COLUMNS) {
                continue;
            }
            questions.add(new Question(row[0], row[1], row[2], row[3], row[4], row[5]));
        }
        return questions;
    }
}
