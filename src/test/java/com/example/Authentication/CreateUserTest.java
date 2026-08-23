package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CreateUserTest {

    @Autowired
    private CreateUser createUser;

    @Autowired
    private PlayerDetailsService players;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    void writesTheUserWhereSignInLooksForIt() {
        assertThat(createUser.makeUser(new String[] {"Ada Lovelace", "make_ada", "correct horse"})).isTrue();
        assertThat(signsIn("make_ada", "correct horse")).isTrue();
    }

    @Test
    void neverStoresThePasswordItself() {
        createUser.makeUser(new String[] {"Ada Lovelace", "make_secret", "correct horse"});

        assertThat(users.findByUsername("make_secret"))
                .hasValueSatisfying(stored -> assertThat(stored.passwordHash()).doesNotContain("correct horse"));
    }

    @Test
    void refusesAUsernameThatIsTaken() {
        assertThat(createUser.makeUser(new String[] {"Ada Lovelace", "make_taken", "one"})).isTrue();
        assertThat(createUser.makeUser(new String[] {"Ada Byron", "make_taken", "two"})).isFalse();

        assertThat(signsIn("make_taken", "one")).isTrue();
        assertThat(signsIn("make_taken", "two")).isFalse();
    }

    @Test
    void keepsTheUsersThatWereAlreadyThere() {
        createUser.makeUser(new String[] {"Ada Lovelace", "make_ada2", "one"});
        createUser.makeUser(new String[] {"Grace Hopper", "make_grace", "two"});

        assertThat(signsIn("make_ada2", "one")).isTrue();
        assertThat(signsIn("make_grace", "two")).isTrue();
    }

    @Test
    void theStoredHashIsBcrypt() {
        createUser.makeUser(new String[] {"Ada Lovelace", "make_bcrypt", "correct horse"});

        assertThat(users.findByUsername("make_bcrypt"))
                .hasValueSatisfying(stored -> assertThat(stored.passwordHash()).startsWith("$2a$"));
    }

    /** Raw BCrypt, with no {id} prefix. A DelegatingPasswordEncoder would reject every one of them. */
    @Test
    void theStoredHashCarriesNoEncoderPrefix() {
        createUser.makeUser(new String[] {"Ada Lovelace", "make_prefix", "correct horse"});

        assertThat(users.findByUsername("make_prefix"))
                .hasValueSatisfying(stored -> assertThat(stored.passwordHash()).doesNotStartWith("{"));
    }

    @Test
    void twoUsersWithTheSamePasswordGetDifferentHashes() {
        createUser.makeUser(new String[] {"Ada Lovelace", "make_same1", "same"});
        createUser.makeUser(new String[] {"Grace Hopper", "make_same2", "same"});

        assertThat(users.findByUsername("make_same1").orElseThrow().passwordHash())
                .isNotEqualTo(users.findByUsername("make_same2").orElseThrow().passwordHash());
    }

    @Test
    void theEncoderIsBcryptAndNotADelegatingOne() {
        assertThat(encoder.encode("correct horse")).startsWith("$2a$");
    }

    /** What the framework will do with the row: load it, then match the password against it. */
    private boolean signsIn(String username, String password) {
        try {
            return encoder.matches(password, players.loadUserByUsername(username).getPassword());
        } catch (UsernameNotFoundException unknown) {
            return false;
        }
    }
}
