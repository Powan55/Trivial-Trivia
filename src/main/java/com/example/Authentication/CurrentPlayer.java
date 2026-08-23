package com.example.Authentication;

/**
 * Whoever is playing right now -- either a signed-in player or a guest.
 *
 * <p>Was {@code User}, renamed to stop it colliding with Spring Security's own {@code User}.</p>
 */
public interface CurrentPlayer {

    /** The stored username, or {@code null} for a guest. */
    String getUsername();

    /** A one-line description of who is playing, for the header and the stats page. */
    String getUserInfo();

    boolean isAuthenticated();

    /** Whether this player may replace the question set. */
    boolean isAdmin();
}
