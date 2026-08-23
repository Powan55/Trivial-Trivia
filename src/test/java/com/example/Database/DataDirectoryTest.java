package com.example.Database;

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
    void aSecondStartLeavesTheDirectoryAlone(@TempDir Path tmp) {
        new DataDirectory(tmp.toString());
        assertThat(new DataDirectory(tmp.toString()).root()).isEqualTo(tmp);
    }

    @Test
    void blankConfigurationFallsBackToTheHomeDirectory(@TempDir Path tmp) {
        String home = System.getProperty("user.home");
        System.setProperty("user.home", tmp.toString());
        try {
            assertThat(new DataDirectory("").root()).isEqualTo(tmp.resolve(".trivial-trivia"));
            assertThat(new DataDirectory(null).root()).isEqualTo(tmp.resolve(".trivial-trivia"));
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
