package com.example.Database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CSVAdapterTest {

    private final CSVAdapter csv = new CSVAdapter();

    private static List<String[]> rows(String[]... rows) {
        return new ArrayList<>(List.of(rows));
    }

    private String written(List<String[]> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        csv.write(out, rows);
        return out.toString(StandardCharsets.UTF_8);
    }

    private List<String[]> roundTrip(List<String[]> rows) {
        return csv.read(new ByteArrayInputStream(written(rows).getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void whatIsWrittenIsWhatIsReadBack() {
        assertThat(roundTrip(rows(
                new String[] {"name", "username"},
                new String[] {"Ada Lovelace", "ada"})))
                .containsExactly(new String[] {"name", "username"}, new String[] {"Ada Lovelace", "ada"});
    }

    @Test
    void survivesCommasQuotesAndNewlinesInsideAField() {
        String[] awkward = {"Paris, France", "He said \"hi\"", "line one\nline two"};
        assertThat(roundTrip(rows(awkward))).containsExactly(awkward);
    }

    @Test
    void survivesNonAsciiText() {
        String[] unicode = {"Who wrote Hamlet?", "Shakespeare", "Molière", "夏目漱石"};
        assertThat(roundTrip(rows(unicode))).containsExactly(unicode);
    }

    @Test
    void anEmptyStreamReadsAsEmptyRatherThanThrowing() {
        assertThat(csv.read(new ByteArrayInputStream(new byte[0]))).isEmpty();
    }

    @Test
    void writingAnEmptyListWritesNothing() {
        assertThat(written(new ArrayList<>())).isEmpty();
    }

    @Test
    void aBrokenStreamReadsAsEmptyRatherThanThrowing() {
        assertThat(csv.read(new ByteArrayInputStream("\"unterminated".getBytes(StandardCharsets.UTF_8))))
                .isEmpty();
    }
}
