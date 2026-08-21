package com.example.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
public class JSONAdapter implements Database {

    private static final Logger logger = Logger.getLogger(JSONAdapter.class.getName());

    private final DataDirectory dataDirectory;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public JSONAdapter(DataDirectory dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        Path path = dataDirectory.resolve(fileName);
        if (!Files.exists(path)) {
            logger.warning("No such data file, returning nothing: " + path);
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(Arrays.asList(mapper.readValue(path.toFile(), String[][].class)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading " + path, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        Path path = dataDirectory.resolve(fileName);
        try {
            mapper.writeValue(path.toFile(), data);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing " + path, e);
        }
    }
}
