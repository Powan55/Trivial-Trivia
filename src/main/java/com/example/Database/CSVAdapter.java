package com.example.Database;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Reads and writes the application's data files as CSV.
 *
 * <p>Both directions go through {@link DataDirectory}, so a file written here is the file read
 * back here. They used to disagree: reads resolved against the classpath and writes against the
 * working directory.</p>
 *
 * <p>Not a bean: {@link FileDatabase} owns one of these and picks it by file extension.</p>
 */

public class CSVAdapter implements Database {

    private static final Logger logger = Logger.getLogger(CSVAdapter.class.getName());

    private final DataDirectory dataDirectory;

    public CSVAdapter(DataDirectory dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>();
        Path path = dataDirectory.resolve(fileName);
        if (!Files.exists(path)) {
            logger.warning("No such data file, returning nothing: " + path);
            return data;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVReader csv = new CSVReader(reader)) {
            String[] line;
            while ((line = csv.readNext()) != null) {
                data.add(line);
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "Error reading " + path, e);
            return new ArrayList<>();
        }
        return data;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        Path path = dataDirectory.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVWriter csv = new CSVWriter(writer)) {
            csv.writeAll(data);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing " + path, e);
        }
    }
}
