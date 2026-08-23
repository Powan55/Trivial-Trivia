package com.example.Database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * The three adapters have to agree: whatever goes in comes back out the same, in any format.
 */
class FormatAdapterTest {

    @TempDir
    Path tmp;

    private final RowFormats formats = new RowFormats();

    static Stream<String> formats() {
        return RowFormats.FORMATS.stream();
    }

    private static List<String[]> rows(String[]... rows) {
        return new ArrayList<>(List.of(rows));
    }

    private String write(String format, List<String[]> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formats.of(format).write(out, rows);
        return out.toString(StandardCharsets.UTF_8);
    }

    private List<String[]> read(String format, String document) {
        return formats.of(format).read(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
    }

    @ParameterizedTest
    @MethodSource("formats")
    void whatIsWrittenIsWhatIsReadBack(String format) {
        List<String[]> original = rows(
                new String[] {"Question", "Option1", "Option2", "Option3", "Option4", "Answer"},
                new String[] {"Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris"});

        assertThat(read(format, write(format, original)))
                .containsExactly(original.get(0), original.get(1));
    }

    @ParameterizedTest
    @MethodSource("formats")
    void survivesAwkwardCharacters(String format) {
        String[] awkward = {"Paris, France", "He said \"hi\"", "a & b < c > d", "夏目漱石", "emoji 🎯"};
        assertThat(read(format, write(format, rows(awkward)))).containsExactly(awkward);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void anEmptyStreamReadsAsEmpty(String format) {
        assertThat(read(format, "")).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aMalformedFileDoesNotThrow(String format) {
        assertThatCode(() -> read(format, "{[<not really any of these formats"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {RowFormats.JSON, RowFormats.XML})
    void aMalformedStructuredFileReadsAsEmpty(String format) {
        // CSV has no such thing as a syntax error on a single unquoted line, so it is not here.
        assertThat(read(format, "{[<not really any of these formats")).isEmpty();
    }

    @Test
    void aFileWrittenAsOneFormatCanBeReadBackAsAnother() {
        List<String[]> original = rows(
                new String[] {"Question", "Answer"},
                new String[] {"Capital of France?", "Paris"});

        String asXml = write(RowFormats.XML, read(RowFormats.JSON, write(RowFormats.JSON, original)));

        assertThat(read(RowFormats.XML, asXml)).containsExactly(original.get(0), original.get(1));
    }

    @Test
    void anUnknownExtensionIsTreatedAsCsv() {
        assertThat(formats.forFileName("mystery.dat")).isSameAs(formats.of(RowFormats.CSV));
        assertThat(formats.of("yaml")).isSameAs(formats.of(RowFormats.CSV));
        assertThat(formats.of(null)).isSameAs(formats.of(RowFormats.CSV));
    }

    @Test
    void extensionsAreReadCaseInsensitively() {
        assertThat(RowFormats.extensionOf("shouty.JSON")).isEqualTo("json");
        assertThat(RowFormats.extensionOf("none")).isEmpty();
        assertThat(RowFormats.extensionOf(null)).isEmpty();
        assertThat(formats.forFileName("shouty.JSON")).isSameAs(formats.of(RowFormats.JSON));
    }

    /** An XML document that tries to read a local file through an external entity. */
    @Test
    void theXmlParserDoesNotResolveExternalEntities() throws Exception {
        Path secret = tmp.resolve("secret.txt");
        Files.writeString(secret, "tell-nobody");
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE rows [ <!ENTITY xxe SYSTEM "%s"> ]>
                <rows><row><cell>&xxe;</cell></row></rows>
                """.formatted(secret.toUri());

        // Doctype declarations are refused outright, so this parses to nothing at all.
        assertThat(read(RowFormats.XML, xxe)).isEmpty();
    }

    @Test
    void jsonIsWrittenAsRowsNotAsColumnObjects() {
        assertThat(write(RowFormats.JSON, rows(new String[] {"one", "two"})))
                .contains("\"one\"").doesNotContain("column0");
    }

    @Test
    void xmlIsWrittenAsRowsOfCells() {
        assertThat(write(RowFormats.XML, rows(new String[] {"one", "two"})))
                .contains("<rows>").contains("<row>").contains("<cell>one</cell>");
    }
}
