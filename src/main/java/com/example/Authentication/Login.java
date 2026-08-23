package com.example.Authentication;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Checks a username and password against the stored users.
 */
@Component
public class Login {

    private static final Logger logger = Logger.getLogger(Login.class.getName());

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public Login(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
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
        Optional<UserRepository.StoredUser> stored = users.findByUsername(username);
        if (stored.isPresent() && passwordEncoder.matches(password, stored.get().passwordHash())) {
            return new RealUser(stored.get().displayName(), stored.get().username());
        }
        logger.log(Level.INFO, "Rejected a sign-in for an unknown user or a bad password");
        return null;
    }
}
