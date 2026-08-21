package com.example.Authentication;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.example.Database.CSVAdapter;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class LoginTest {

    @TempDir
    Path tmp;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private CSVAdapter csv;
    private Login login;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
        login = new Login(csv, encoder);
        csv.writeFile(DataFiles.USERS, new ArrayList<>(List.<String[]>of(
                new String[] {"Ada Lovelace", "ada", encoder.encode("correct horse")})));
    }

    @Test
    void acceptsTheRightPasswordAndHandsBackTheStoredUser() {
        RealUser user = login.authenticate("ada", "correct horse");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("ada");
        assertThat(user.getUserInfo()).contains("Ada Lovelace");
    }

    @Test
    void rejectsTheWrongPassword() {
        assertThat(login.authenticate("ada", "Correct horse")).isNull();
    }

    @Test
    void rejectsAnUnknownUser() {
        assertThat(login.authenticate("grace", "correct horse")).isNull();
    }

    @Test
    void rejectsNulls() {
        assertThat(login.authenticate(null, "correct horse")).isNull();
        assertThat(login.authenticate("ada", null)).isNull();
    }

    @Test
    void rejectsEverythingWhenThereAreNoUsers() {
        csv.writeFile(DataFiles.USERS, new ArrayList<>());
        assertThat(login.authenticate("ada", "correct horse")).isNull();
    }

    @Test
    void aTruncatedRowIsSkippedRatherThanThrowing() {
        csv.writeFile(DataFiles.USERS, new ArrayList<>(List.<String[]>of(new String[] {"Ada", "ada"})));
        assertThat(login.authenticate("ada", "correct horse")).isNull();
    }
}
