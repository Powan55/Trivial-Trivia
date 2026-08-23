package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /**
     * BCrypt at the library default cost. It carries its own salt, so no salt column.
     *
     * <p>Explicit, and it has to stay explicit: every stored hash is raw BCrypt with no
     * {@code {id}} prefix, and Spring's default {@code DelegatingPasswordEncoder} would reject
     * all of them at once. Backfill the prefixes in a migration before changing this.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
