package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PlayerDetailsServiceTest {

    @Autowired
    private PlayerDetailsService players;

    @Autowired
    private UserRepository users;

    @Test
    void loadsAStoredPlayerWithTheHashAndTheRole() {
        users.insert("Ada Lovelace", "details_ada", "$2a$10$hash");

        UserDetails loaded = players.loadUserByUsername("details_ada");

        assertThat(loaded.getUsername()).isEqualTo("details_ada");
        assertThat(loaded.getPassword()).isEqualTo("$2a$10$hash");
        assertThat(loaded.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.isAccountNonLocked()).isTrue();
    }

    @Test
    void aPromotedPlayerComesBackAsAnAdministrator() {
        users.insert("Ada Lovelace", "details_admin", "$2a$10$hash");
        assertThat(users.promoteToAdmin("details_admin")).isTrue();

        assertThat(players.loadUserByUsername("details_admin").getAuthorities())
                .extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void anUnknownUsernameIsNotFoundRatherThanNull() {
        assertThatThrownBy(() -> players.loadUserByUsername("details_nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void promotingAnAccountThatIsNotThereChangesNothing() {
        assertThat(users.promoteToAdmin("details_nobody")).isFalse();
        assertThat(users.promoteToAdmin(null)).isFalse();
        assertThat(users.promoteToAdmin("  ")).isFalse();
    }

    /** The throttle is reported through the locked flag, which is checked before the password. */
    @Test
    void aThrottledPlayerComesBackLocked() {
        users.insert("Ada Lovelace", "details_locked", "$2a$10$hash");
        LoginAttemptService blocked = new LoginAttemptService() {
            @Override
            public boolean isBlocked(String username) {
                return true;
            }
        };

        assertThat(new PlayerDetailsService(users, blocked)
                .loadUserByUsername("details_locked").isAccountNonLocked()).isFalse();
    }

    @Test
    void thePromotionRunnerPromotesTheNamedAccount() {
        users.insert("Ada Lovelace", "details_promoted", "$2a$10$hash");

        new AdminPromotion(users, "details_promoted").promote();

        assertThat(players.loadUserByUsername("details_promoted").getAuthorities())
                .extracting("authority").containsExactly("ROLE_ADMIN");
    }

    /**
     * A boot failure rather than a warning, because the alternative is a variable pointed at a
     * free username on an instance that cold-starts all day: whoever registers it next would be
     * an administrator.
     */
    @Test
    void thePromotionRunnerStopsTheContextWhenThereIsNoSuchAccount() {
        AdminPromotion promotion = new AdminPromotion(users, "details_never_registered");

        assertThatThrownBy(promotion::promote)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has not registered");
    }
}
