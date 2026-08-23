package com.example.Database;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * One rendering of a table of strings: CSV, JSON or XML.
 *
 * <p>This used to be called {@code Database} and used to take file names, because the three
 * adapters behind it were the application's datastore. They are not any more. What they are good
 * at is the feature the README advertises -- export the question set in three formats, import it
 * back in any of them -- and that job is a stream in and a stream out. Taking bytes rather than
 * paths is what lets export stop writing a scratch file into a directory the hosted instance
 * does not have.</p>
 */
public interface RowFormat {

    /** Parses rows out of an upload. Returns nothing rather than throwing on a malformed file. */
    List<String[]> read(InputStream in);

    void write(OutputStream out, List<String[]> rows);
}
