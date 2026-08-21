package com.example.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataDirectoryTest {

    @Test
    void createsTheDirectoryIfItIsNotThere(@TempDir Path tmp) {
        Path root = tmp.resolve("nested/data");
        new DataDirectory(root.toString());
        assertThat(root).isDirectory();
    }

    @Test
    void seedsTheQuestionsOutOfTheClasspath(@TempDir Path tmp) throws IOException {
        new DataDirectory(tmp.toString());
        Path questions = tmp.resolve(DataFiles.QUESTIONS);
        assertThat(questions).isRegularFile();
        assertThat(Files.readString(questions)).startsWith("Question,Option1");
    }

    @Test
    void createsTheFilesTheAppWritesTo(@TempDir Path tmp) {
        new DataDirectory(tmp.toString());
        assertThat(tmp.resolve(DataFiles.USERS)).isRegularFile();
        assertThat(tmp.resolve(DataFiles.STATS)).isRegularFile();
    }

    @Test
    void aSecondStartLeavesExistingDataAlone(@TempDir Path tmp) throws IOException {
        new DataDirectory(tmp.toString());
        Path users = tmp.resolve(DataFiles.USERS);
        Files.writeString(users, "\"Ada\",\"ada\",\"hash\",\"salt\"\n");
        Files.writeString(tmp.resolve(DataFiles.QUESTIONS), "Question,Option1\n");

        new DataDirectory(tmp.toString());

        assertThat(Files.readString(users)).contains("ada");
        assertThat(Files.readString(tmp.resolve(DataFiles.QUESTIONS))).isEqualTo("Question,Option1\n");
    }

    @Test
    void blankConfigurationFallsBackToTheHomeDirectory(@TempDir Path tmp) {
        String home = System.getProperty("user.home");
        System.setProperty("user.home", tmp.toString());
        try {
            assertThat(new DataDirectory("").root()).isEqualTo(tmp.resolve(".trivial-trivia"));
        } finally {
            System.setProperty("user.home", home);
        }
    }

    @Test
    void refusesANameThatEscapesTheDirectory(@TempDir Path tmp) {
        DataDirectory data = new DataDirectory(tmp.toString());
        assertThatThrownBy(() -> data.resolve("../escaped.csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("escapes");
        assertThatThrownBy(() -> data.resolve("a/../../escaped.csv"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAnEmptyName(@TempDir Path tmp) {
        DataDirectory data = new DataDirectory(tmp.toString());
        assertThatThrownBy(() -> data.resolve("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> data.resolve(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
