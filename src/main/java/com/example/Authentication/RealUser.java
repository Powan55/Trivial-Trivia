package com.example.Authentication;

import java.util.ArrayList;

import com.example.Database.DataFiles;
import com.example.Database.Database;

/**
 * A player who has signed in.
 *
 * <p>Not a Spring bean: one of these is created per successful sign-in and held by the
 * session-scoped {@link ProxyUser}.</p>
 */
public class RealUser implements User {

    private final String name;
    private final String userName;

    public RealUser(String name, String userName) {
        this.name = name;
        this.userName = userName;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getUserInfo() {
        return (name == null || name.isBlank()) ? userName : name + " (" + userName + ")";
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void saveScore(Database database, int score) {
        ArrayList<String[]> stats = database.readFile(DataFiles.STATS);
        stats.add(new String[] {userName, String.valueOf(score)});
        database.writeFile(DataFiles.STATS, stats);
    }
}
