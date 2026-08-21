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

import static org.assertj.core.api.Assertions.assertThat;

class LoginTest {

    @TempDir
    Path tmp;

    private CSVAdapter csv;
    private Login login;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
        login = new Login(csv);
        String salt = PasswordHashing.generateSalt();
        csv.writeFile(DataFiles.USERS, new ArrayList<>(List.<String[]>of(
                new String[] {"Ada Lovelace", "ada", PasswordHashing.hashPassword("correct horse", salt), salt})));
    }

    @Test
    void acceptsTheRightPassword() {
        assertThat(login.authenticate("ada", "correct horse")).isTrue();
    }

    @Test
    void rejectsTheWrongPassword() {
        assertThat(login.authenticate("ada", "Correct horse")).isFalse();
    }

    @Test
    void rejectsAnUnknownUser() {
        assertThat(login.authenticate("grace", "correct horse")).isFalse();
    }

    @Test
    void rejectsNulls() {
        assertThat(login.authenticate(null, "correct horse")).isFalse();
        assertThat(login.authenticate("ada", null)).isFalse();
    }

    @Test
    void rejectsEverythingWhenThereAreNoUsers() {
        csv.writeFile(DataFiles.USERS, new ArrayList<>());
        assertThat(login.authenticate("ada", "correct horse")).isFalse();
    }

    @Test
    void aTruncatedRowIsSkippedRatherThanThrowing() {
        csv.writeFile(DataFiles.USERS, new ArrayList<>(List.<String[]>of(new String[] {"Ada", "ada"})));
        assertThat(login.authenticate("ada", "correct horse")).isFalse();
    }
}
