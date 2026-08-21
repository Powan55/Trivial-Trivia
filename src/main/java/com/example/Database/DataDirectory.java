package com.example.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Resolves the writable directory the application keeps its data in, and seeds it on first run.
 *
 * <p>The application used to read through {@code ClassPathResource} and write through a raw
 * {@code FileOutputStream} relative to the working directory, so a read and a write of the same
 * logical file went to two different places. Everything now goes through here.</p>
 */
@Component
public class DataDirectory {

    private static final Logger logger = Logger.getLogger(DataDirectory.class.getName());

    /** Where the shipped seed copies live on the classpath. */
    private static final String SEED_PREFIX = "Data/";

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
        seed();
        logger.info("Data directory: " + root);
    }

    public Path root() {
        return root;
    }

    /**
     * Resolves a file name against the data directory.
     *
     * @throws IllegalArgumentException if the name would escape the directory. File names reach
     *         this method from user-supplied import and export paths, so this is a boundary check,
     *         not a formality.
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

    /** Copies the shipped questions out of the jar and creates the two files users generate. */
    private void seed() {
        copyIfAbsent(DataFiles.QUESTIONS);
        createIfAbsent(DataFiles.USERS);
        createIfAbsent(DataFiles.STATS);
    }

    private void copyIfAbsent(String fileName) {
        Path target = resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        ClassPathResource seed = new ClassPathResource(SEED_PREFIX + fileName);
        if (!seed.exists()) {
            logger.warning("No seed copy of " + fileName + " on the classpath");
            return;
        }
        try (InputStream in = seed.getInputStream()) {
            Files.copy(in, target);
            logger.info("Seeded " + target);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot seed " + target, e);
        }
    }

    private void createIfAbsent(String fileName) {
        Path target = resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        try {
            Files.createFile(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create " + target, e);
        }
    }
}
