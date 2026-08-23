package com.example.Persistence;

import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * The users table.
 *
 * <p>Registration used to read the whole users file, check the username itself, append a row and
 * write the file back. Six people registering at once left two rows: four accounts were confirmed
 * to their owners and then overwritten. The unique constraint on {@code username} is what makes
 * that impossible now -- {@link #insert} does not look before it leaps, it inserts and lets the
 * database decide who won.</p>
 */
@Repository
public class UserRepository {

    /** A stored user, minus nothing: the hash is here because sign-in needs it. */
    public record StoredUser(long id, String username, String displayName, String passwordHash) {
    }

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** @return false if the username is already taken */
    public boolean insert(String displayName, String username, String passwordHash) {
        try {
            return jdbc.sql("""
                    insert into users (username, display_name, password_hash)
                    values (?, ?, ?)
                    """)
                    .params(username, displayName, passwordHash)
                    .update() == 1;
        } catch (DuplicateKeyException taken) {
            return false;
        }
    }

    public Optional<StoredUser> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return jdbc.sql("""
                select id, username, display_name, password_hash
                from users
                where username = ?
                """)
                .param(username)
                .query((rs, row) -> new StoredUser(rs.getLong("id"), rs.getString("username"),
                        rs.getString("display_name"), rs.getString("password_hash")))
                .optional();
    }

    public long count() {
        return jdbc.sql("select count(*) from users").query(Long.class).single();
    }
}
