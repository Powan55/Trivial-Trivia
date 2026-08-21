package com.example.Authentication;

import java.nio.file.Path;

import com.example.Database.CSVAdapter;
import com.example.Database.DataDirectory;
import com.example.Database.DataFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyUserTest {

    @TempDir
    Path tmp;

    private CSVAdapter csv;
    private ProxyUser proxy;

    @BeforeEach
    void setUp() {
        csv = new CSVAdapter(new DataDirectory(tmp.toString()));
        proxy = new ProxyUser();
    }

    @Test
    void startsAsAGuest() {
        assertThat(proxy.isAuthenticated()).isFalse();
        assertThat(proxy.getUsername()).isNull();
        assertThat(proxy.getUserInfo()).isEqualTo("Guest");
    }

    @Test
    void aGuestScoreIsNotStored() {
        proxy.saveScore(csv, 40);
        assertThat(csv.readFile(DataFiles.STATS)).isEmpty();
    }

    @Test
    void signingInSwitchesToTheRealUser() {
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));
        assertThat(proxy.isAuthenticated()).isTrue();
        assertThat(proxy.getUsername()).isEqualTo("ada");
        assertThat(proxy.getUserInfo()).isEqualTo("Ada Lovelace (ada)");
    }

    @Test
    void aSignedInScoreIsStoredAgainstTheUsername() {
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));
        proxy.saveScore(csv, 40);
        assertThat(csv.readFile(DataFiles.STATS)).containsExactly(new String[] {"ada", "40"});
    }

    @Test
    void signingOutGoesBackToBeingAGuest() {
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));
        proxy.signOut();
        assertThat(proxy.isAuthenticated()).isFalse();
        assertThat(proxy.getUserInfo()).isEqualTo("Guest");
    }

    /** No shared state between instances -- ProxyUser used to hold the current user statically. */
    @Test
    void oneProxyDoesNotSeeAnother() {
        ProxyUser other = new ProxyUser();
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));

        assertThat(other.isAuthenticated()).isFalse();
        assertThat(other.getUsername()).isNull();
    }

    @Test
    void aUserWithNoStoredNameFallsBackToTheUsername() {
        proxy.signIn(new RealUser("", "ada"));
        assertThat(proxy.getUserInfo()).isEqualTo("ada");
    }
}
