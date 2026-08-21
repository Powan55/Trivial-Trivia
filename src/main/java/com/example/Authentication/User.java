package com.example.Authentication;

import com.example.Database.Database;

/**
 * Whoever is playing right now -- either a signed-in player or a guest.
 */
public interface User {

    /** The stored username, or {@code null} for a guest. */
    String getUsername();

    /** A one-line description of who is playing, for the stats page. */
    String getUserInfo();

    boolean isAuthenticated();

    /** Appends a finished round's score to the stats file. A guest's score is not stored. */
    void saveScore(Database database, int score);
}
