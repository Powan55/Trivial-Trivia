package com.example.Database;

import com.example.Command.LoginAction;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.OutputStream;

@Component
public class CSVAdapter implements Database {

    private static final Logger logger = Logger.getLogger(LoginAction.class.getName());

    @Autowired
    private ResourceLoader resourceLoader;

    private ServletContext servletContext;

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("Data/" + fileName);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     CSVReader reader = new CSVReader(new InputStreamReader(is))) {

                    String[] line;
                    while ((line = reader.readNext()) != null) {
                        data.add(line);
                    }
                }
            } else {
                logger.log(Level.SEVERE, "File not found: " + fileName);
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "Error reading the file: " + fileName, e);
        }
        return data;
    }



    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        try (OutputStream os = new FileOutputStream( fileName);
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(os))) {
            for (String[] row : data) {
                writer.writeNext(row);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to the file: " + fileName, e);
        }
    }

}

