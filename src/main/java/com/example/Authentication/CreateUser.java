package com.example.Authentication;

import java.util.ArrayList;

import com.example.Database.DataFiles;
import com.example.Database.Database;
import org.springframework.stereotype.Component;

/**
 * Creates a user and appends it to the stored users.
 *
 * @author Laxmi Poudel
 */
@Component
public class CreateUser {

    private static final int USERNAME = 1;
    private static final int COLUMNS = 4;

    private final Database database;

    public CreateUser(Database database) {
        this.database = database;
    }

    /**
     * @param info name, username, password -- in that order
     * @return false if the username is taken
     */
    public boolean makeUser(String[] info) {
        ArrayList<String[]> users = database.readFile(DataFiles.USERS);
        if (!isUniqueUser(info[1], users)) {
            return false;
        }

        String salt = PasswordHashing.generateSalt();
        String hash = PasswordHashing.hashPassword(info[2], salt);

        users.add(new String[] {info[0], info[1], hash, salt});
        database.writeFile(DataFiles.USERS, users);
        return true;
    }

    public boolean isUniqueUser(String username, ArrayList<String[]> users) {
        for (String[] row : users) {
            if (row.length >= COLUMNS && row[USERNAME].equals(username)) {
                return false;
            }
        }
        return true;
    }
}
