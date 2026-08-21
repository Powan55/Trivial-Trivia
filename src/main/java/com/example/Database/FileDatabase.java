package com.example.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Picks the format from the file extension, so callers name a file and stop caring.
 *
 * <p>Anything without a recognised extension is read and written as CSV, which is what the
 * application's own data files are.</p>
 */
@Component
public class FileDatabase implements Database {

    public static final String CSV = "csv";
    public static final String JSON = "json";
    public static final String XML = "xml";

    /** The extensions import and export accept. */
    public static final List<String> FORMATS = List.of(CSV, JSON, XML);

    private final Database csv;
    private final Database json;
    private final Database xml;

    public FileDatabase(DataDirectory dataDirectory) {
        this.csv = new CSVAdapter(dataDirectory);
        this.json = new JSONAdapter(dataDirectory);
        this.xml = new XMLAdapter(dataDirectory);
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        return adapterFor(fileName).readFile(fileName);
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        adapterFor(fileName).writeFile(fileName, data);
    }

    public static String extensionOf(String fileName) {
        int dot = fileName == null ? -1 : fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private Database adapterFor(String fileName) {
        return switch (extensionOf(fileName)) {
            case JSON -> json;
            case XML -> xml;
            default -> csv;
        };
    }
}
