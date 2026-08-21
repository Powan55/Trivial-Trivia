package com.example.Authentication;

import java.nio.file.Path;
import java.util.ArrayList;

import com.example.Database.CSVAdapter;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserTest {

    @TempDir
    Path tmp;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private CSVAdapter csv;
    private CreateUser createUser;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
        createUser = new CreateUser(csv, encoder);
    }

    @Test
    void writesTheUserWhereLoginLooksForIt() {
        assertThat(createUser.makeUser(new String[] {"Ada Lovelace", "ada", "correct horse"})).isTrue();
        assertThat(new Login(csv, encoder).authenticate("ada", "correct horse")).isNotNull();
    }

    @Test
    void neverStoresThePasswordItself() {
        createUser.makeUser(new String[] {"Ada Lovelace", "ada", "correct horse"});
        assertThat(csv.readFile(DataFiles.USERS).get(0)).doesNotContain("correct horse");
    }

    @Test
    void refusesAUsernameThatIsTaken() {
        assertThat(createUser.makeUser(new String[] {"Ada Lovelace", "ada", "one"})).isTrue();
        assertThat(createUser.makeUser(new String[] {"Ada Byron", "ada", "two"})).isFalse();
        assertThat(csv.readFile(DataFiles.USERS)).hasSize(1);
    }

    @Test
    void keepsTheUsersThatWereAlreadyThere() {
        createUser.makeUser(new String[] {"Ada Lovelace", "ada", "one"});
        createUser.makeUser(new String[] {"Grace Hopper", "grace", "two"});

        assertThat(csv.readFile(DataFiles.USERS)).hasSize(2);
        assertThat(new Login(csv, encoder).authenticate("ada", "one")).isNotNull();
        assertThat(new Login(csv, encoder).authenticate("grace", "two")).isNotNull();
    }

    @Test
    void theStoredHashIsBcrypt() {
        createUser.makeUser(new String[] {"Ada Lovelace", "ada", "correct horse"});
        assertThat(csv.readFile(DataFiles.USERS).get(0)[2]).startsWith("$2a$");
    }

    @Test
    void twoUsersWithTheSamePasswordGetDifferentHashes() {
        createUser.makeUser(new String[] {"Ada Lovelace", "ada", "same"});
        createUser.makeUser(new String[] {"Grace Hopper", "grace", "same"});

        ArrayList<String[]> users = csv.readFile(DataFiles.USERS);
        assertThat(users.get(0)[2]).isNotEqualTo(users.get(1)[2]);
    }
}
