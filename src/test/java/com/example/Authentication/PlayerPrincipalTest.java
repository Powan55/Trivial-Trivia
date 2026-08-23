package com.example.Authentication;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerPrincipalTest {

    @Test
    void describesItselfWithTheNameAndTheUsername() {
        assertThat(player("Ada Lovelace").describe()).isEqualTo("Ada Lovelace (ada)");
    }

    @Test
    void fallsBackToTheUsernameWhenThereIsNoName() {
        assertThat(player("").describe()).isEqualTo("ada");
        assertThat(player(null).describe()).isEqualTo("ada");
        assertThat(player("   ").describe()).isEqualTo("ada");
    }

    @Test
    void carriesItsRoleAsAnAuthority() {
        assertThat(player("Ada").getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(player("Ada").isAdmin()).isFalse();

        PlayerPrincipal admin = new PlayerPrincipal("ada", "Ada", "hash", PlayerPrincipal.ADMIN, true, true);
        assertThat(admin.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(admin.isAdmin()).isTrue();
    }

    /** A row with no role stored is an ordinary player, not an unauthorised one. */
    @Test
    void aMissingRoleIsAnOrdinaryPlayer() {
        assertThat(new PlayerPrincipal("ada", "Ada", "hash", null, true, true).getAuthorities())
                .extracting("authority").containsExactly("ROLE_USER");
        assertThat(new PlayerPrincipal("ada", "Ada", "hash", " ", true, true).isAdmin()).isFalse();
    }

    @Test
    void theHashIsErasedOnceAuthenticationIsDone() {
        PlayerPrincipal player = player("Ada");
        assertThat(player.getPassword()).isEqualTo("hash");

        player.eraseCredentials();

        assertThat(player.getPassword()).isNull();
    }

    @Test
    void aNewlyRegisteredPlayerCarriesNoHashAtAll() {
        PlayerPrincipal registered = PlayerPrincipal.newlyRegistered("ada", "Ada Lovelace");

        assertThat(registered.getPassword()).isNull();
        assertThat(registered.getUsername()).isEqualTo("ada");
        assertThat(registered.describe()).isEqualTo("Ada Lovelace (ada)");
        assertThat(registered.isAdmin()).isFalse();
        assertThat(registered.isEnabled()).isTrue();
        assertThat(registered.isAccountNonLocked()).isTrue();
    }

    @Test
    void reportsTheFlagsTheFrameworkChecksBeforeThePassword() {
        PlayerPrincipal locked = new PlayerPrincipal("ada", "Ada", "hash", PlayerPrincipal.USER, false, false);

        assertThat(locked.isEnabled()).isFalse();
        assertThat(locked.isAccountNonLocked()).isFalse();
        assertThat(locked.isAccountNonExpired()).isTrue();
        assertThat(locked.isCredentialsNonExpired()).isTrue();
    }

    private static PlayerPrincipal player(String displayName) {
        return new PlayerPrincipal("ada", displayName, "hash", PlayerPrincipal.USER, true, true);
    }
}
