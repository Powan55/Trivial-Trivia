package com.example.Database;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Picks the format from a file extension, so callers name a file and stop caring.
 *
 * <p>Anything without a recognised extension is read and written as CSV.</p>
 */
@Component
public class RowFormats {

    public static final String CSV = "csv";
    public static final String JSON = "json";
    public static final String XML = "xml";

    /** The extensions import and export accept. */
    public static final List<String> FORMATS = List.of(CSV, JSON, XML);

    private final RowFormat csv = new CSVAdapter();
    private final RowFormat json = new JSONAdapter();
    private final RowFormat xml = new XMLAdapter();

    public static String extensionOf(String fileName) {
        int dot = fileName == null ? -1 : fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public RowFormat of(String format) {
        return switch (format == null ? "" : format.toLowerCase(Locale.ROOT)) {
            case JSON -> json;
            case XML -> xml;
            default -> csv;
        };
    }

    /** The format for a file name, by its extension. */
    public RowFormat forFileName(String fileName) {
        return of(extensionOf(fileName));
    }
}
