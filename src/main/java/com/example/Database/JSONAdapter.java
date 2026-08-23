package com.example.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The same rows as an array of arrays:
 *
 * <pre>
 * [["Question","Option1"],["Capital of France?","London"]]
 * </pre>
 *
 * <p>Rows, not objects. The version of this that never shipped invented keys called
 * {@code column0}, {@code column1} and so on, which is a worse CSV rather than better JSON.</p>
 */
public class JSONAdapter implements RowFormat {

    private static final Logger logger = Logger.getLogger(JSONAdapter.class.getName());

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public List<String[]> read(InputStream in) {
        try {
            return new ArrayList<>(Arrays.asList(mapper.readValue(in, String[][].class)));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read the uploaded JSON", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void write(OutputStream out, List<String[]> rows) {
        try {
            mapper.writeValue(out, rows);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write JSON", e);
        }
    }
}
