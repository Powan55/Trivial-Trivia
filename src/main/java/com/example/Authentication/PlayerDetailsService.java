package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a player for the framework to authenticate. Replaces the hand-rolled {@code Login}.
 *
 * <p>Its existence is also what stops Boot printing a generated password to stdout on every
 * boot, which on a hosted instance is a live credential in the deploy log.</p>
 */
@Service
public class PlayerDetailsService implements UserDetailsService {

    private final UserRepository users;
    private final LoginAttemptService attempts;

    public PlayerDetailsService(UserRepository users, LoginAttemptService attempts) {
        this.users = users;
        this.attempts = attempts;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserRepository.StoredUser stored = users.findByUsername(username)
                // The caller is not told which half was wrong: DaoAuthenticationProvider turns
                // this into the same BadCredentialsException a wrong password gets.
                .orElseThrow(() -> new UsernameNotFoundException("No such user"));
        return new PlayerPrincipal(stored.username(), stored.displayName(), stored.passwordHash(),
                stored.role(), stored.enabled(), !attempts.isBlocked(username));
    }
}
