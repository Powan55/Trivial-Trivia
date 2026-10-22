package com.example.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import com.example.Database.RowFormats;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Moves a 1.0 install's CSV files into the database, once, on a developer's machine.
 *
 * <p>Development profile only. The hosted instance has no disk and never seeded the users or
 * stats files, so there has never been a production row to migrate and there is no way for one to
 * appear. A permanent production code path for data that does not exist is a path nobody audits;
 * this one lives where the two laptops that need it are. It goes away in 2.1.</p>
 *
 * <p>The condition is an empty users table, with no marker file. A fresh database next to old
 * CSVs re-imports, which is what anyone would want; a database with users in it is never touched,
 * which is the part that matters. The CSVs are left where they are, so rolling the binary back
 * still works.</p>
 */
@Component
@Profile("dev")
public class LegacyDataMigrator implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(LegacyDataMigrator.class.getName());

    /** userData.csv: name, username, bcrypt hash. */
    private static final int USER_COLUMNS = 3;
    /** StatData.csv: username, score. */
    private static final int STAT_COLUMNS = 2;

    private final DataDirectory dataDirectory;
    private final RowFormats formats;
    private final UserRepository users;
    private final RoundRepository rounds;
    private final QuestionRepository questions;

    public LegacyDataMigrator(DataDirectory dataDirectory, RowFormats formats, UserRepository users,
                              RoundRepository rounds, QuestionRepository questions) {
        this.dataDirectory = dataDirectory;
        this.formats = formats;
        this.users = users;
        this.rounds = rounds;
        this.questions = questions;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        int imported = importUsers();
        if (imported == 0) {
            return;
        }
        logger.info("Imported " + imported + " account(s) from " + DataFiles.USERS);
        importStats();
        importQuestions();
    }

    private int importUsers() {
        int imported = 0;
        for (String[] row : read(DataFiles.USERS)) {
            if (row.length < USER_COLUMNS || row[1].isBlank()) {
                continue;
            }
            if (insertQuietly(row[0], row[1], row[2])) {
                imported++;
            }
        }
        return imported;
    }

    private boolean insertQuietly(String name, String username, String hash) {
        try {
            // Hashes are already BCrypt and copy across untouched. Re-hashing is not possible:
            // the cleartext is gone, which is the point of storing a hash.
            return users.insert(name, username, hash);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Skipped a legacy user row that the database refused", e);
            return false;
        }
    }

    private void importStats() {
        for (String[] row : read(DataFiles.STATS)) {
            if (row.length < STAT_COLUMNS) {
                continue;
            }
            try {
                // 1.0 stored a score and nothing else, so the answer tallies start at zero.
                rounds.record(row[0], Integer.parseInt(row[1].trim()), 0, 0);
            } catch (NumberFormatException handEdited) {
                logger.warning("Skipped a legacy score row that is not a number");
            }
        }
    }

    private void importQuestions() {
        List<String[]> rows = read(DataFiles.QUESTIONS);
        if (rows.size() < 2) {
            return;
        }
        try {
            questions.replaceAll(rows);
            logger.info("Imported " + (rows.size() - 1) + " question(s) from " + DataFiles.QUESTIONS);
        } catch (IllegalArgumentException invalid) {
            logger.warning("Left the shipped questions alone: " + invalid.getMessage());
        }
    }

    private List<String[]> read(String fileName) {
        Path path = dataDirectory.resolve(fileName);
        if (!Files.exists(path)) {
            return List.of();
        }
        try (InputStream in = Files.newInputStream(path)) {
            return formats.forFileName(fileName).read(in);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read " + path, e);
            return List.of();
        }
    }
}
