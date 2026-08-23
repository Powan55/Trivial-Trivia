package com.example.Database;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the writable directory the application keeps local state in.
 *
 * <p>It used to seed three CSV files here and treat them as the datastore. The datastore is now a
 * database; what is left in this directory is the development H2 file and whatever legacy CSVs a
 * 1.0 install left behind for {@link com.example.Persistence.LegacyDataMigrator} to read.</p>
 */
@Component
public class DataDirectory {

    private static final Logger logger = Logger.getLogger(DataDirectory.class.getName());

    private final Path root;

    public DataDirectory(@Value("${app.data.dir:}") String configured) {
        this.root = configured == null || configured.isBlank()
                ? Paths.get(System.getProperty("user.home"), ".trivial-trivia")
                : Paths.get(configured);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create the data directory at " + root, e);
        }
        logger.info("Data directory: " + root);
    }

    public Path root() {
        return root;
    }

    /**
     * Resolves a file name against the data directory.
     *
     * @throws IllegalArgumentException if the name would escape the directory
     */
    public Path resolve(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        Path resolved = root.resolve(fileName).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("File name escapes the data directory: " + fileName);
        }
        return resolved;
    }
}
