package com.example.api.v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generates the OpenAPI document from the real controllers rather than hand-maintaining one [25].
 * A springdoc misconfiguration or an un-annotated endpoint fails this test instead of quietly
 * publishing a stale or empty file from {@code pages.yml}.
 *
 * <p>{@code /v3/api-docs} matches no route in {@code SecurityConfig} and so falls under
 * {@code anyRequest().authenticated()} -- signing a throwaway user in first is the same
 * requirement any other authenticated {@code /api/v1} caller has, not a security config change
 * made just for this test.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OpenApiDocumentTest.HistoryDescriptions.class)
class OpenApiDocumentTest {

    private static final Path OUTPUT = Path.of("target", "openapi", "openapi.json");

    @Autowired
    private TestRestTemplate rest;

    @Test
    void theGeneratedDocumentCoversTheApiV1Surface() throws IOException {
        ResponseEntity<String> form = rest.getForEntity("/createUser", String.class);
        Map<String, String> jar = new LinkedHashMap<>();
        collectCookies(jar, form);
        String token = tokenIn(form.getBody());

        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        registerHeaders.add(HttpHeaders.COOKIE, cookieHeader(jar));
        String registration = """
                {"name": "OpenAPI Test", "username": "openapi_doc_user", "password": "correct horse"}
                """;
        ResponseEntity<String> registered = rest.exchange("/api/v1/users?_csrf=" + token,
                HttpMethod.POST, new HttpEntity<>(registration, registerHeaders), String.class);
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        collectCookies(jar, registered);

        HttpHeaders docsHeaders = new HttpHeaders();
        docsHeaders.add(HttpHeaders.COOKIE, cookieHeader(jar));
        ResponseEntity<String> response = rest.exchange("/v3/api-docs", HttpMethod.GET,
                new HttpEntity<>(docsHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotBlank();

        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, body);

        assertThat(body).contains("\"/api/v1/users/me\"")
                .contains("\"/api/v1/questions\"")
                .contains("\"/api/v1/questions/export\"")
                .contains("\"/api/v1/rounds\"")
                .contains("\"/api/v1/users\"");

        JsonNode document = new ObjectMapper().readTree(body);
        JsonNode registerResponses = document
                .path("paths").path("/api/v1/users").path("post").path("responses");
        assertThat(registerResponses.fieldNames()).toIterable()
                .as("the documented response codes for POST /api/v1/users")
                .contains("201");

        JsonNode schemas = document.path("components").path("schemas");
        assertThat(schemas.path("ScoreHistoryDto").path("properties").path("rounds")
                .path("items").path("$ref").asText()).isEqualTo("#/components/schemas/RoundHistoryDto");
        JsonNode history = schemas.path("RoundHistoryDto").path("properties");
        assertThat(history.fieldNames()).toIterable().containsExactlyInAnyOrder("score", "finishedAt", "correctCount", "totalQuestions");
        assertThat(history.path("finishedAt").path("description").asText())
                .contains("local date and time", "server time", "without an offset", "null");
        assertThat(history.path("finishedAt").has("format")).isFalse();
        for (String field : List.of("finishedAt", "correctCount", "totalQuestions")) {
            assertThat(history.path(field).path("type")).anySatisfy(type -> assertThat(type.asText()).isEqualTo("null"));
            assertThat(history.path(field).path("description").asText()).contains("null");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class HistoryDescriptions {
        @Bean
        OpenApiCustomizer historyDescriptions() {
            return document -> {
                io.swagger.v3.oas.models.media.Schema<?> history = document.getComponents().getSchemas().get("RoundHistoryDto");
                var properties = history.getProperties();
                properties.get("finishedAt").setDescription("ISO-8601 local date and time in server time, without an offset; null when unavailable.");
                properties.get("correctCount").setDescription("Saved correct answer count; null when answer counts are unavailable.");
                properties.get("totalQuestions").setDescription("Saved question count, or positive legacy answer total; null when unavailable.");
            };
        }
    }

    /**
     * A real Set-Cookie list now carries both JSESSIONID and XSRF-TOKEN, so the jar has to merge
     * by name rather than trust whichever header happened to come last -- and has to drop a
     * cookie a response deletes (empty value), since CsrfAuthenticationStrategy deletes the CSRF
     * cookie on sign-in and only regenerates it on the next request, not the sign-in response
     * itself.
     */
    private static void collectCookies(Map<String, String> jar, ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            return;
        }
        for (String cookie : cookies) {
            String pair = cookie.substring(0, cookie.indexOf(';'));
            String name = pair.substring(0, pair.indexOf('='));
            String value = pair.substring(pair.indexOf('=') + 1);
            if (value.isEmpty()) {
                jar.remove(name);
            } else {
                jar.put(name, value);
            }
        }
    }

    private static String cookieHeader(Map<String, String> jar) {
        return jar.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
    }

    private static String tokenIn(String body) {
        Matcher token = Pattern.compile("name=\"_csrf\" value=\"([^\"]*)\"").matcher(body);
        assertThat(token.find()).as("a hidden token in the page").isTrue();
        return token.group(1);
    }
}
