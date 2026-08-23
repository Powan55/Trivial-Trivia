package com.example.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

/** Rows as CSV. */
public class CSVAdapter implements RowFormat {

    private static final Logger logger = Logger.getLogger(CSVAdapter.class.getName());

    @Override
    public List<String[]> read(InputStream in) {
        List<String[]> data = new ArrayList<>();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVReader csv = new CSVReader(reader)) {
            String[] line;
            while ((line = csv.readNext()) != null) {
                data.add(line);
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.WARNING, "Could not read the uploaded CSV", e);
            return new ArrayList<>();
        }
        return data;
    }

    @Override
    public void write(OutputStream out, List<String[]> rows) {
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        try (CSVWriter csv = new CSVWriter(writer)) {
            csv.writeAll(rows);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write CSV", e);
        }
    }
}
