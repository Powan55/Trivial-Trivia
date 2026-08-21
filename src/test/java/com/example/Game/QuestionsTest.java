package com.example.Game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.example.Database.CSVAdapter;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionsTest {

    private static final String[] HEADER =
            {"Question", "Option1", "Option2", "Option3", "Option4", "Answer"};

    @TempDir
    Path tmp;

    private CSVAdapter csv;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
    }

    private void given(String[]... rows) {
        csv.writeFile(DataFiles.QUESTIONS, new ArrayList<>(List.of(rows)));
    }

    @Test
    void readsTheRowsBelowTheHeader() {
        given(HEADER,
                new String[] {"Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris"},
                new String[] {"Red planet?", "Venus", "Jupiter", "Mars", "Saturn", "Mars"});

        List<Question> questions = new Questions(csv).getQuestion();

        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).getQuestion()).isEqualTo("Capital of France?");
        assertThat(questions.get(0).getOption(4)).isEqualTo("Paris");
        assertThat(questions.get(1).getAnswer()).isEqualTo("Mars");
    }

    @Test
    void aHeaderOnlyFileYieldsNothing() {
        given(HEADER);
        assertThat(new Questions(csv).getQuestion()).isEmpty();
    }

    @Test
    void anEmptyFileYieldsNothingRatherThanThrowing() {
        csv.writeFile(DataFiles.QUESTIONS, new ArrayList<>());
        assertThat(new Questions(csv).getQuestion()).isEmpty();
    }

    @Test
    void aMissingFileYieldsNothingRatherThanThrowing() throws IOException {
        Files.delete(tmp.resolve(DataFiles.QUESTIONS));
        assertThat(new Questions(csv).getQuestion()).isEmpty();
    }

    @Test
    void theShippedSeedParses() {
        // DataDirectory copies Data/QuestionData.csv out of the jar on first run; if that file
        // ever stops matching the six-column layout, this is where it shows up.
        assertThat(new Questions(csv).getQuestion()).isNotEmpty();
    }

    @Test
    void aRowWithTooFewColumnsIsSkipped() {
        given(HEADER,
                new String[] {"Truncated", "a", "b"},
                new String[] {"Red planet?", "Venus", "Jupiter", "Mars", "Saturn", "Mars"});

        List<Question> questions = new Questions(csv).getQuestion();

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getQuestion()).isEqualTo("Red planet?");
    }
}
