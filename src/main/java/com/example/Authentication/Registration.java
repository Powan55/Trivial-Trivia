package com.example.Authentication;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Checks a registration form before anything is stored.
 *
 * <p>The browser's {@code required} attributes are a convenience, not a check -- anything can POST
 * to the endpoint directly.</p>
 */
public final class Registration {

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 20;
    public static final int PASSWORD_MIN = 8;
    public static final int NAME_MAX = 60;

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]+");

    private Registration() {
    }

    /**
     * @return the first problem found, or empty if the form is usable
     */
    public static Optional<String> validate(String name, String username, String password) {
        if (isBlank(name)) {
            return Optional.of("Enter your name.");
        }
        if (name.length() > NAME_MAX) {
            return Optional.of("Names are limited to " + NAME_MAX + " characters.");
        }
        if (isBlank(username)) {
            return Optional.of("Choose a username.");
        }
        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            return Optional.of("Usernames are " + USERNAME_MIN + " to " + USERNAME_MAX + " characters.");
        }
        if (!USERNAME.matcher(username).matches()) {
            return Optional.of("Usernames can use letters, numbers and underscores only.");
        }
        if (password == null || password.length() < PASSWORD_MIN) {
            return Optional.of("Passwords need at least " + PASSWORD_MIN + " characters.");
        }
        return Optional.empty();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
