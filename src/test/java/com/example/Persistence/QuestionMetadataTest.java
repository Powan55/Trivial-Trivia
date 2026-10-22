package com.example.Persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import com.example.Database.RowFormats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class QuestionMetadataTest {
    @Autowired QuestionRepository questions;
    private List<String[]> original;
    private static final String[] ROW = {"Capital?", "A", "B", "C", "D", "A", " Science ", " hard "};

    @BeforeEach
    void remember() { original = questions.export(); }

    @AfterEach
    void restore() { questions.replaceAll(original); }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json", "xml"})
    void metadataRoundTripsAcrossEveryFormat(String format) throws Exception {
        var adapter = new RowFormats().of(format);
        var bytes = new ByteArrayOutputStream();
        adapter.write(bytes, List.of(QuestionRepository.HEADER, ROW));
        questions.replaceAll(adapter.read(new ByteArrayInputStream(bytes.toByteArray())));
        assertThat(questions.findAll().get(0).getCategory()).isEqualTo("Science");
        assertThat(questions.findAll().get(0).getDifficulty()).isEqualTo("HARD");
        bytes.reset();
        adapter.write(bytes, questions.export());
        var rows = adapter.read(new ByteArrayInputStream(bytes.toByteArray()));
        assertThat(rows.get(0)).containsExactly(QuestionRepository.HEADER);
        assertThat(rows.get(1)).hasSize(8).endsWith("Science", "HARD");
    }

    @Test
    void oldRowsAndBlankMetadataHaveSharedDefaults() {
        questions.replaceAll(List.of(QuestionRepository.LEGACY_HEADER, Arrays.copyOf(ROW, 6)));
        assertThat(questions.export().get(1)).endsWith("Uncategorized", "MEDIUM");
        String[] blank = ROW.clone(); blank[6] = "  "; blank[7] = "";
        questions.replaceAll(List.of(QuestionRepository.HEADER, blank));
        assertThat(questions.export().get(1)).endsWith("Uncategorized", "MEDIUM");
    }

    @Test
    void malformedRowsNeverReplaceAnyExistingData() {
        for (int cell = 0; cell < 8; cell++) {
            String[] bad = ROW.clone(); bad[cell] = null;
            rejects(bad);
        }
        for (int cell = 0; cell < 6; cell++) {
            String[] bad = ROW.clone(); bad[cell] = "  ";
            rejects(bad);
        }
        rejects(Arrays.copyOf(ROW, 7));
        rejects(Arrays.copyOf(ROW, 9));
        String[] bad = ROW.clone(); bad[6] = "x".repeat(61); rejects(bad);
        bad = ROW.clone(); bad[7] = "IMPOSSIBLE"; rejects(bad);
        bad = ROW.clone(); bad[5] = "missing option"; rejects(bad);
        assertThatThrownBy(() -> questions.replaceAll(List.of(new String[]{"wrong header"}, ROW)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void rejects(String[] bad) {
        assertThatThrownBy(() -> questions.replaceAll(List.of(QuestionRepository.HEADER, ROW, bad)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(questions.export()).usingRecursiveComparison().isEqualTo(original);
    }
}
