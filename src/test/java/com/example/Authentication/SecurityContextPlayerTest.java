package com.example.Authentication;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextPlayerTest {

    private final SecurityContextPlayer player = new SecurityContextPlayer();

    @AfterEach
    void clearTheContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anEmptyContextIsAGuest() {
        assertThat(player.isAuthenticated()).isFalse();
        assertThat(player.getUsername()).isNull();
        assertThat(player.getUserInfo()).isEqualTo("Guest");
        assertThat(player.isAdmin()).isFalse();
    }

    /** What ProxyUser with a null user was modelling by hand. */
    @Test
    void anAnonymousTokenIsAGuestToo() {
        set(new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThat(player.isAuthenticated()).isFalse();
        assertThat(player.getUsername()).isNull();
        assertThat(player.getUserInfo()).isEqualTo("Guest");
        assertThat(player.isAdmin()).isFalse();
    }

    @Test
    void aSignedInPlayerIsReadOffThePrincipal() {
        PlayerPrincipal ada = new PlayerPrincipal("ada", "Ada Lovelace", null, PlayerPrincipal.USER, true, true);
        set(UsernamePasswordAuthenticationToken.authenticated(ada, null, ada.getAuthorities()));

        assertThat(player.isAuthenticated()).isTrue();
        assertThat(player.getUsername()).isEqualTo("ada");
        assertThat(player.getUserInfo()).isEqualTo("Ada Lovelace (ada)");
        assertThat(player.isAdmin()).isFalse();
    }

    @Test
    void anAdministratorIsRecognisedByTheAuthority() {
        PlayerPrincipal ada = new PlayerPrincipal("ada", "Ada", null, PlayerPrincipal.ADMIN, true, true);
        set(UsernamePasswordAuthenticationToken.authenticated(ada, null, ada.getAuthorities()));

        assertThat(player.isAdmin()).isTrue();
    }

    /** Some other framework put a plain string in there: fall back to the name, not a crash. */
    @Test
    void aPrincipalOfAnotherKindStillHasAName() {
        set(UsernamePasswordAuthenticationToken.authenticated("someone", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(player.getUserInfo()).isEqualTo("someone");
        assertThat(player.getUsername()).isEqualTo("someone");
    }

    @Test
    void anUnauthenticatedTokenIsNotSignedIn() {
        set(UsernamePasswordAuthenticationToken.unauthenticated("ada", "correct horse"));

        assertThat(player.isAuthenticated()).isFalse();
        assertThat(player.getUserInfo()).isEqualTo("Guest");
    }

    private static void set(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
