package com.example.Database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
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

    private DataDirectory data;
    private FileDatabase files;

    @BeforeEach
    void setUp() {
        data = new DataDirectory(tmp.toString());
        files = new FileDatabase(data);
    }

    static Stream<String> formats() {
        return FileDatabase.FORMATS.stream();
    }

    private static ArrayList<String[]> rows(String[]... rows) {
        return new ArrayList<>(List.of(rows));
    }

    @ParameterizedTest
    @MethodSource("formats")
    void whatIsWrittenIsWhatIsReadBack(String format) {
        ArrayList<String[]> original = rows(
                new String[] {"Question", "Option1", "Option2", "Option3", "Option4", "Answer"},
                new String[] {"Capital of France?", "London", "Berlin", "Madrid", "Paris", "Paris"});

        files.writeFile("questions." + format, original);

        assertThat(files.readFile("questions." + format))
                .containsExactly(original.get(0), original.get(1));
    }

    @ParameterizedTest
    @MethodSource("formats")
    void survivesAwkwardCharacters(String format) {
        String[] awkward = {"Paris, France", "He said \"hi\"", "a & b < c > d", "夏目漱石", "emoji 🎯"};
        files.writeFile("awkward." + format, rows(awkward));
        assertThat(files.readFile("awkward." + format)).containsExactly(awkward);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aMissingFileReadsAsEmpty(String format) {
        assertThat(files.readFile("nothing-here." + format)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aMalformedFileDoesNotThrow(String format) throws Exception {
        Files.writeString(data.resolve("broken." + format), "{[<not really any of these formats");
        assertThatCode(() -> files.readFile("broken." + format)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {FileDatabase.JSON, FileDatabase.XML})
    void aMalformedStructuredFileReadsAsEmpty(String format) throws Exception {
        // CSV has no such thing as a syntax error on a single unquoted line, so it is not here.
        Files.writeString(data.resolve("broken2." + format), "{[<not really any of these formats");
        assertThat(files.readFile("broken2." + format)).isEmpty();
    }

    @Test
    void aFileWrittenAsOneFormatCanBeReadBackAsAnother() {
        ArrayList<String[]> original = rows(
                new String[] {"Question", "Answer"},
                new String[] {"Capital of France?", "Paris"});

        files.writeFile("source.json", original);
        files.writeFile("target.xml", files.readFile("source.json"));

        assertThat(files.readFile("target.xml")).containsExactly(original.get(0), original.get(1));
    }

    @Test
    void anUnknownExtensionIsTreatedAsCsv() {
        files.writeFile("mystery.dat", rows(new String[] {"a", "b"}));
        assertThat(Files.exists(data.resolve("mystery.dat"))).isTrue();
        assertThat(files.readFile("mystery.dat")).containsExactly(new String[] {"a", "b"});
    }

    @Test
    void extensionsAreReadCaseInsensitively() {
        files.writeFile("shouty.JSON", rows(new String[] {"a", "b"}));
        // If .JSON had fallen through to CSV this would be quoted text, not a JSON array.
        assertThat(readRaw("shouty.JSON")).startsWith("[");
    }

    /** An XML document that tries to read a local file through an external entity. */
    @Test
    void theXmlParserDoesNotResolveExternalEntities() throws Exception {
        Path secret = tmp.resolve("secret.txt");
        Files.writeString(secret, "tell-nobody");
        Files.writeString(data.resolve("xxe.xml"), """
                <?xml version="1.0"?>
                <!DOCTYPE rows [ <!ENTITY xxe SYSTEM "%s"> ]>
                <rows><row><cell>&xxe;</cell></row></rows>
                """.formatted(secret.toUri()));

        // Doctype declarations are refused outright, so this parses to nothing at all.
        assertThat(files.readFile("xxe.xml")).isEmpty();
    }

    @Test
    void jsonIsWrittenAsRowsNotAsColumnObjects() {
        files.writeFile("shape.json", rows(new String[] {"one", "two"}));
        assertThat(readRaw("shape.json")).contains("\"one\"").doesNotContain("column0");
    }

    @Test
    void xmlIsWrittenAsRowsOfCells() {
        files.writeFile("shape.xml", rows(new String[] {"one", "two"}));
        assertThat(readRaw("shape.xml")).contains("<rows>").contains("<row>").contains("<cell>one</cell>");
    }

    private String readRaw(String fileName) {
        try {
            return Files.readString(data.resolve(fileName));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
