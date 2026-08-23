package com.example.Persistence;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

/**
 * Translates a host's {@code DATABASE_URL} into the properties Spring actually binds.
 *
 * <p>Two things go wrong here and both go wrong silently. Spring Boot does not bind
 * {@code DATABASE_URL} at all -- relaxed binding maps {@code SPRING_DATASOURCE_URL} and nothing
 * else -- so an app handed a PostgreSQL instance starts on an embedded H2, migrates that, and
 * says nothing about it. And what a host hands out is {@code postgres://user:pass@host/db}, which
 * is not a JDBC URL: Spring rejects it with {@code 'url' must start with "jdbc"}.</p>
 *
 * <p>This is the one named place that translation happens. It contributes the lowest-precedence
 * property source in the environment, so an explicit {@code SPRING_DATASOURCE_URL} still wins.</p>
 */
public class DatabaseUrl implements EnvironmentPostProcessor {

    static final String SOURCE_NAME = "translatedDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> translated = translate(environment.getProperty("DATABASE_URL"));
        if (!translated.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, translated));
            return;
        }
        if (environment.getProperty("spring.datasource.url") == null
                && environment.acceptsProfiles(Profiles.of("prod"))) {
            // Boot's own message for a missing URL suggests putting an embedded database on the
            // classpath. H2 is already on it, and starting on it here is the data loss this
            // milestone exists to stop, so say what to set instead.
            throw new IllegalStateException("""
                    The production profile has no database. Set SPRING_DATASOURCE_URL to a jdbc: \
                    URL, or DATABASE_URL to the postgres:// URI the host hands out. Starting on \
                    the embedded H2 instead would lose every account on the next restart.""");
        }
    }

    /** @return the datasource properties a {@code postgres://} URI means, or nothing */
    static Map<String, Object> translate(String databaseUrl) {
        if (databaseUrl == null) {
            return Map.of();
        }
        String trimmed = databaseUrl.trim();
        // Anything else is somebody else's variable. A malformed postgres:// URL still throws,
        // because that one is a misconfigured deploy rather than a coincidence of naming.
        if (!trimmed.startsWith("postgres://") && !trimmed.startsWith("postgresql://")) {
            return Map.of();
        }
        URI uri = URI.create(trimmed);
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() != -1) {
            jdbc.append(':').append(uri.getPort());
        }
        if (uri.getPath() != null) {
            jdbc.append(uri.getPath());
        }
        if (uri.getQuery() != null) {
            jdbc.append('?').append(uri.getQuery());
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", jdbc.toString());
        String credentials = uri.getUserInfo();
        if (credentials != null) {
            int colon = credentials.indexOf(':');
            properties.put("spring.datasource.username",
                    colon < 0 ? credentials : credentials.substring(0, colon));
            if (colon >= 0) {
                properties.put("spring.datasource.password", credentials.substring(colon + 1));
            }
        }
        return properties;
    }
}
