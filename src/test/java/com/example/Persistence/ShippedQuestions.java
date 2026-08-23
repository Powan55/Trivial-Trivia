package com.example.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.example.Database.CSVAdapter;

/** The question set the build ships, read straight off the classpath. */
final class ShippedQuestions {

    private ShippedQuestions() {
    }

    static List<String[]> rows() {
        try (InputStream in = ShippedQuestions.class.getResourceAsStream("/Data/QuestionData.csv")) {
            return new CSVAdapter().read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
