package com.example.Authentication;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Database.DataFiles;
import com.example.Database.Database;
import org.springframework.stereotype.Component;

/**
 * Checks a username and password against the stored users.
 */
@Component
public class Login {

    private static final Logger logger = Logger.getLogger(Login.class.getName());

    /** Column layout of userData.csv: name, username, hash, salt. */
    private static final int USERNAME = 1;
    private static final int HASH = 2;
    private static final int SALT = 3;
    private static final int COLUMNS = 4;

    private final Database database;

    public Login(Database database) {
        this.database = database;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        for (String[] row : List.copyOf(database.readFile(DataFiles.USERS))) {
            if (row.length < COLUMNS || !row[USERNAME].equals(username)) {
                continue;
            }
            if (row[HASH].equals(PasswordHashing.hashPassword(password, row[SALT]))) {
                return true;
            }
        }
        logger.log(Level.INFO, "Rejected a sign-in for an unknown user or a bad password");
        return false;
    }
}
