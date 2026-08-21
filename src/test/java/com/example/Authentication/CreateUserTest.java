package com.example.Authentication;

import java.nio.file.Path;
import java.util.ArrayList;

import com.example.Database.CSVAdapter;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserTest {

    @TempDir
    Path tmp;

    private CSVAdapter csv;
    private CreateUser createUser;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
        createUser = new CreateUser(csv);
    }

    @Test
    void writesTheUserWhereLoginLooksForIt() {
        assertThat(createUser.makeUser(new String[] {"Ada Lovelace", "ada", "correct horse"})).isTrue();
        assertThat(new Login(csv).authenticate("ada", "correct horse")).isTrue();
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
        assertThat(new Login(csv).authenticate("ada", "one")).isTrue();
        assertThat(new Login(csv).authenticate("grace", "two")).isTrue();
    }

    @Test
    void twoUsersWithTheSamePasswordGetDifferentHashes() {
        createUser.makeUser(new String[] {"Ada Lovelace", "ada", "same"});
        createUser.makeUser(new String[] {"Grace Hopper", "grace", "same"});

        ArrayList<String[]> users = csv.readFile(DataFiles.USERS);
        assertThat(users.get(0)[2]).isNotEqualTo(users.get(1)[2]);
    }
}
