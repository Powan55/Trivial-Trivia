package com.example.Authentication;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Database.DataFiles;
import com.example.Database.Database;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Checks a username and password against the stored users.
 */
@Component
public class Login {

    private static final Logger logger = Logger.getLogger(Login.class.getName());

    /** Column layout of userData.csv: name, username, bcrypt hash. */
    private static final int NAME = 0;
    private static final int USERNAME = 1;
    private static final int HASH = 2;
    private static final int COLUMNS = 3;

    private final Database database;
    private final PasswordEncoder passwordEncoder;

    public Login(Database database, PasswordEncoder passwordEncoder) {
        this.database = database;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return the matching user, or {@code null} if the username is unknown or the password is
     *         wrong. The caller is not told which -- that difference is useful to an attacker.
     */
    public RealUser authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        for (String[] row : List.copyOf(database.readFile(DataFiles.USERS))) {
            if (row.length < COLUMNS || !row[USERNAME].equals(username)) {
                continue;
            }
            if (passwordEncoder.matches(password, row[HASH])) {
                return new RealUser(row[NAME], row[USERNAME]);
            }
        }
        logger.log(Level.INFO, "Rejected a sign-in for an unknown user or a bad password");
        return null;
    }
}
