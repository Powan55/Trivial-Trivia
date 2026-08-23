package com.example.Authentication;

/**
 * Whoever is playing right now -- either a signed-in player or a guest.
 */
public interface User {

    /** The stored username, or {@code null} for a guest. */
    String getUsername();

    /** A one-line description of who is playing, for the stats page. */
    String getUserInfo();

    boolean isAuthenticated();
}
