package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LoginTest {

    @Autowired
    private Login login;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void givenAStoredUser() {
        users.insert("Ada Lovelace", "login_ada", encoder.encode("correct horse"));
    }

    @Test
    void acceptsTheRightPasswordAndHandsBackTheStoredUser() {
        RealUser user = login.authenticate("login_ada", "correct horse");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("login_ada");
        assertThat(user.getUserInfo()).contains("Ada Lovelace");
    }

    @Test
    void rejectsTheWrongPassword() {
        assertThat(login.authenticate("login_ada", "Correct horse")).isNull();
    }

    @Test
    void rejectsAnUnknownUser() {
        assertThat(login.authenticate("login_nobody", "correct horse")).isNull();
    }

    @Test
    void rejectsNulls() {
        assertThat(login.authenticate(null, "correct horse")).isNull();
        assertThat(login.authenticate("login_ada", null)).isNull();
    }

    /** A hash written before this milestone has to keep working, verbatim, with no re-hashing. */
    @Test
    void acceptsAHashThatWasStoredByTheOldFileDatastore() {
        String legacy = "$2a$10$jApN1zYYVxlVAgHbqfZEOuKLm2kDtC09Baej4iQVWi.GLpJw/zE0q";
        users.insert("Legacy", "login_legacy", legacy);

        assertThat(login.authenticate("login_legacy", "correct horse")).isNotNull();
        assertThat(login.authenticate("login_legacy", "wrong")).isNull();
    }
}
