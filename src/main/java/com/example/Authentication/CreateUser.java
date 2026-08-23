package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a user.
 *
 * <p>There is no "is this username free?" read here on purpose. Checking and then inserting is
 * two statements with a gap between them, and that gap is exactly where six simultaneous
 * registrations turned into two stored accounts. The unique constraint decides, and losing the
 * race is an ordinary false rather than an exception the caller has to know about.</p>
 *
 * @author Laxmi Poudel
 */
@Component
public class CreateUser {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public CreateUser(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param info name, username, password -- in that order
     * @return false if the username is taken
     */
    public boolean makeUser(String[] info) {
        return users.insert(info[0], info[1], passwordEncoder.encode(info[2]));
    }
}
