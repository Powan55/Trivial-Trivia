package com.example.Authentication;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Stands in for whoever is playing, over the security context.
 *
 * <p>Still the proxy {@code ProxyUser} was, but the thing behind it is now the framework's
 * per-request authentication rather than a mutable session field. A guest is an
 * {@link AnonymousAuthenticationToken}, which is what the old null-user case modelled by hand.</p>
 */
@Component
public class SecurityContextPlayer implements CurrentPlayer {

    @Override
    public String getUsername() {
        Authentication signedIn = signedIn();
        return signedIn == null ? null : signedIn.getName();
    }

    @Override
    public String getUserInfo() {
        Authentication signedIn = signedIn();
        if (signedIn == null) {
            return "Guest";
        }
        return signedIn.getPrincipal() instanceof PlayerPrincipal player
                ? player.describe()
                : signedIn.getName();
    }

    @Override
    public boolean isAuthenticated() {
        return signedIn() != null;
    }

    @Override
    public boolean isAdmin() {
        Authentication signedIn = signedIn();
        return signedIn != null && signedIn.getAuthorities().stream()
                .anyMatch(granted -> PlayerPrincipal.ADMIN.equals(granted.getAuthority()));
    }

    private static Authentication signedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }
}
