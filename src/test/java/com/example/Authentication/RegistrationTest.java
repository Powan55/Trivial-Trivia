package com.example.Authentication;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationTest {

    @Test
    void acceptsAUsableForm() {
        assertThat(Registration.validate("Ada Lovelace", "ada_l", "correct horse")).isEmpty();
    }

    @Test
    void wantsAName() {
        assertThat(Registration.validate("  ", "ada_l", "correct horse")).contains("Enter your name.");
        assertThat(Registration.validate(null, "ada_l", "correct horse")).isPresent();
    }

    @Test
    void capsTheNameLength() {
        assertThat(Registration.validate("a".repeat(61), "ada_l", "correct horse")).isPresent();
        assertThat(Registration.validate("a".repeat(60), "ada_l", "correct horse")).isEmpty();
    }

    @Test
    void wantsAUsername() {
        assertThat(Registration.validate("Ada", "", "correct horse")).contains("Choose a username.");
    }

    @Test
    void enforcesTheUsernameLength() {
        assertThat(Registration.validate("Ada", "ad", "correct horse")).isPresent();
        assertThat(Registration.validate("Ada", "a".repeat(21), "correct horse")).isPresent();
        assertThat(Registration.validate("Ada", "ada", "correct horse")).isEmpty();
    }

    @Test
    void rejectsUsernamesThatArentLettersNumbersOrUnderscores() {
        assertThat(Registration.validate("Ada", "ada lovelace", "correct horse")).isPresent();
        assertThat(Registration.validate("Ada", "<script>", "correct horse")).isPresent();
        assertThat(Registration.validate("Ada", "ada,l", "correct horse")).isPresent();
    }

    @Test
    void enforcesAPasswordFloor() {
        assertThat(Registration.validate("Ada", "ada_l", "sevench")).isPresent();
        assertThat(Registration.validate("Ada", "ada_l", null)).isPresent();
        assertThat(Registration.validate("Ada", "ada_l", "eightchr")).isEmpty();
    }
}
