package com.example.Authentication;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private Instant now;
    private LoginAttemptService attempts;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-01-01T00:00:00Z");
        attempts = new LoginAttemptService(new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return now;
            }
        });
    }

    @Test
    void anUnknownUsernameIsNotBlocked() {
        assertThat(attempts.isBlocked("nobody")).isFalse();
    }

    @Test
    void oneFailureShortOfTheLimitIsStillAllowedThrough() {
        fail("ada", LoginAttemptService.MAX_FAILURES - 1);
        assertThat(attempts.isBlocked("ada")).isFalse();
    }

    @Test
    void theLimitLocksTheUsernameOut() {
        fail("ada", LoginAttemptService.MAX_FAILURES);
        assertThat(attempts.isBlocked("ada")).isTrue();
    }

    @Test
    void oneUsernameLockedOutDoesNotLockOutAnother() {
        fail("ada", LoginAttemptService.MAX_FAILURES);
        assertThat(attempts.isBlocked("grace")).isFalse();
    }

    @Test
    void theLockoutExpires() {
        fail("ada", LoginAttemptService.MAX_FAILURES);
        now = now.plus(LoginAttemptService.LOCKOUT).plusSeconds(1);

        assertThat(attempts.isBlocked("ada")).isFalse();
    }

    @Test
    void failuresOlderThanTheLockoutDoNotAddUp() {
        fail("ada", LoginAttemptService.MAX_FAILURES - 1);
        now = now.plus(LoginAttemptService.LOCKOUT).plusSeconds(1);
        fail("ada", 1);

        assertThat(attempts.isBlocked("ada")).isFalse();
    }

    @Test
    void signingInSuccessfullyForgivesTheFailures() {
        fail("ada", LoginAttemptService.MAX_FAILURES);
        attempts.onSuccess(new AuthenticationSuccessEvent(
                UsernamePasswordAuthenticationToken.unauthenticated("ada", "correct horse")));

        assertThat(attempts.isBlocked("ada")).isFalse();
    }

    /** Usernames are whatever was typed, so the map cannot be allowed to grow with them. */
    @Test
    void theMapStopsGrowingAndKeepsTheLockoutsItAlreadyHas() {
        fail("ada", LoginAttemptService.MAX_FAILURES);
        for (int i = 0; i < LoginAttemptService.MAX_TRACKED + 10; i++) {
            fail("flood_" + i, 1);
        }

        assertThat(attempts.isBlocked("ada")).isTrue();
    }

    /**
     * The flood is not the attack, it is the setup: fill the map with usernames that never earn a
     * lockout and every real account becomes unthrottleable. Evicting the entries that never
     * counted is what keeps the cost of filling it at MAX_FAILURES posts per entry.
     */
    @Test
    void aFloodedMapStillLocksOutAFreshUsername() {
        for (int i = 0; i < LoginAttemptService.MAX_TRACKED + 10; i++) {
            fail("flood_" + i, 1);
        }

        fail("ada", LoginAttemptService.MAX_FAILURES);

        assertThat(attempts.isBlocked("ada")).isTrue();
    }

    /** A full map still drops the entries whose lockout has run out. */
    @Test
    void expiredEntriesMakeRoomOnceTheMapIsFull() {
        for (int i = 0; i < LoginAttemptService.MAX_TRACKED; i++) {
            fail("old_" + i, 1);
        }
        now = now.plus(LoginAttemptService.LOCKOUT).plusSeconds(1);
        fail("ada", LoginAttemptService.MAX_FAILURES);

        assertThat(attempts.isBlocked("ada")).isTrue();
    }

    private void fail(String username, int times) {
        AuthenticationException rejected = new UsernameNotFoundException("no");
        for (int attempt = 0; attempt < times; attempt++) {
            attempts.onFailure(new AuthenticationFailureBadCredentialsEvent(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, "guess"), rejected));
        }
    }
}
