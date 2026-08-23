package com.example.Authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyUserTest {

    private ProxyUser proxy;

    @BeforeEach
    void setUp() {
        proxy = new ProxyUser();
    }

    @Test
    void startsAsAGuest() {
        assertThat(proxy.isAuthenticated()).isFalse();
        assertThat(proxy.getUsername()).isNull();
        assertThat(proxy.getUserInfo()).isEqualTo("Guest");
    }

    @Test
    void signingInSwitchesToTheRealUser() {
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));
        assertThat(proxy.isAuthenticated()).isTrue();
        assertThat(proxy.getUsername()).isEqualTo("ada");
        assertThat(proxy.getUserInfo()).isEqualTo("Ada Lovelace (ada)");
    }

    @Test
    void signingOutGoesBackToBeingAGuest() {
        proxy.signIn(new RealUser("Ada Lovelace", "ada"));
        proxy.signOut();
        assertThat(proxy.isAuthenticated()).isFalse();
        assertThat(proxy.getUserInfo()).isEqualTo("Guest");
        assertThat(proxy.getUsername()).isNull();
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
        proxy.signIn(new RealUser(null, "ada"));
        assertThat(proxy.getUserInfo()).isEqualTo("ada");
    }
}
