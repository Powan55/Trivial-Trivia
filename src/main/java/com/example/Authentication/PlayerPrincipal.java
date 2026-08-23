package com.example.Authentication;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A stored player, in the shape the security framework wants.
 *
 * <p>Carries the display name so rendering the header does not cost a query, and is a class
 * rather than a record because {@link CredentialsContainer} has to be able to null the hash out
 * once authentication is done. Without that the BCrypt hash would sit in the HTTP session for as
 * long as the session lives.</p>
 */
public final class PlayerPrincipal implements UserDetails, CredentialsContainer, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";

    private final String username;
    private final String displayName;
    private final String role;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private String passwordHash;

    public PlayerPrincipal(String username, String displayName, String passwordHash, String role,
                           boolean enabled, boolean accountNonLocked) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role == null || role.isBlank() ? USER : role;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    /** Whoever just registered: the row was written a moment ago and the hash is not needed. */
    public static PlayerPrincipal newlyRegistered(String username, String displayName) {
        return new PlayerPrincipal(username, displayName, null, USER, true, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    public boolean isAdmin() {
        return ADMIN.equals(role);
    }

    /** The name if there is one, otherwise the username. */
    public String describe() {
        return displayName == null || displayName.isBlank()
                ? username
                : displayName + " (" + username + ")";
    }
}
