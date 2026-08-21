package com.example.Database;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CSVAdapterTest {

    @TempDir
    Path tmp;

    private CSVAdapter csv;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
    }

    private static ArrayList<String[]> rows(String[]... rows) {
        return new ArrayList<>(List.of(rows));
    }

    @Test
    void whatIsWrittenIsWhatIsReadBack() {
        csv.writeFile("round-trip.csv", rows(
                new String[] {"name", "username"},
                new String[] {"Ada Lovelace", "ada"}));

        assertThat(csv.readFile("round-trip.csv"))
                .containsExactly(new String[] {"name", "username"}, new String[] {"Ada Lovelace", "ada"});
    }

    @Test
    void survivesCommasQuotesAndNewlinesInsideAField() {
        String[] awkward = {"Paris, France", "He said \"hi\"", "line one\nline two"};
        csv.writeFile("awkward.csv", rows(awkward));
        assertThat(csv.readFile("awkward.csv")).containsExactly(awkward);
    }

    @Test
    void survivesNonAsciiText() {
        String[] unicode = {"Who wrote Hamlet?", "Shakespeare", "Molière", "夏目漱石"};
        csv.writeFile("unicode.csv", rows(unicode));
        assertThat(csv.readFile("unicode.csv")).containsExactly(unicode);
    }

    @Test
    void aMissingFileReadsAsEmptyRatherThanThrowing() {
        assertThat(csv.readFile("not-here.csv")).isEmpty();
    }

    @Test
    void writingAnEmptyListLeavesAnEmptyFile() {
        csv.writeFile("empty.csv", new ArrayList<>());
        assertThat(csv.readFile("empty.csv")).isEmpty();
    }

    @Test
    void aWriteReplacesRatherThanAppends() {
        csv.writeFile("once.csv", rows(new String[] {"first"}));
        csv.writeFile("once.csv", rows(new String[] {"second"}));
        assertThat(csv.readFile("once.csv")).containsExactly(new String[] {"second"});
    }

    @Test
    void theFileLandsInTheDataDirectory() {
        csv.writeFile("where.csv", rows(new String[] {"x"}));
        assertThat(tmp.resolve("where.csv")).isRegularFile();
    }
}
