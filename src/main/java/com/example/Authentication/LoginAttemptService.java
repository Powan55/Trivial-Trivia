package com.example.Authentication;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Counts failed sign-ins per username and locks one out for a while.
 *
 * <p>Fed by the framework's own authentication events, so nothing on the sign-in path has to
 * remember to call it. A locked username is reported through
 * {@link PlayerPrincipal#isAccountNonLocked()}, which is checked before the password is, so a
 * lockout costs no BCrypt work.</p>
 */
// The ceiling: one in-memory map, so the counter is per instance and a restart forgives
// everyone. Move it to a table the moment this runs on more than one instance.
@Component
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration LOCKOUT = Duration.ofMinutes(15);
    /** Usernames are whatever was typed, so the map needs a ceiling of its own. */
    static final int MAX_TRACKED = 10_000;

    private record Failures(int count, Instant until) {
    }

    private final Map<String, Failures> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        Instant now = clock.instant();
        if (failures.size() >= MAX_TRACKED) {
            // Drop what expired and what never earned a lockout. Refusing new entries instead
            // would hand an attacker the whole defence: one failure against MAX_TRACKED junk
            // usernames costs nothing, fills the map with counters that never expire early, and
            // from then on no real account can be locked out at all. Evicting the ones below the
            // threshold puts the price of filling it at MAX_FAILURES posts per entry, and every
            // lockout already earned survives.
            failures.values().removeIf(seen -> seen.until().isBefore(now)
                    || seen.count() < MAX_FAILURES);
        }
        failures.compute(username, (key, seen) -> new Failures(
                seen == null || seen.until().isBefore(now) ? 1 : seen.count() + 1,
                now.plus(LOCKOUT)));
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        failures.remove(event.getAuthentication().getName());
    }

    public boolean isBlocked(String username) {
        Failures seen = failures.get(username);
        if (seen == null) {
            return false;
        }
        if (seen.until().isBefore(clock.instant())) {
            failures.remove(username, seen);
            return false;
        }
        return seen.count() >= MAX_FAILURES;
    }
}
