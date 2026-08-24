package com.example.api.v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
class OpenApiDocumentTest {

    private static final Path OUTPUT = Path.of("target", "openapi", "openapi.json");

    @Autowired
    private TestRestTemplate rest;

    @Test
    void theGeneratedDocumentCoversTheApiV1Surface() throws IOException {
        ResponseEntity<String> form = rest.getForEntity("/createUser", String.class);
        String formCookie = cookieIn(form);
        String token = tokenIn(form.getBody());

        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        registerHeaders.add(HttpHeaders.COOKIE, formCookie);
        String registration = """
                {"name": "OpenAPI Test", "username": "openapi_doc_user", "password": "correct horse"}
                """;
        ResponseEntity<String> registered = rest.exchange("/api/v1/users?_csrf=" + token,
                HttpMethod.POST, new HttpEntity<>(registration, registerHeaders), String.class);
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpHeaders docsHeaders = new HttpHeaders();
        docsHeaders.add(HttpHeaders.COOKIE, cookieIn(registered));
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

        JsonNode registerResponses = new ObjectMapper().readTree(body)
                .path("paths").path("/api/v1/users").path("post").path("responses");
        assertThat(registerResponses.fieldNames()).toIterable()
                .as("the documented response codes for POST /api/v1/users")
                .contains("201");
    }

    private static String cookieIn(ResponseEntity<String> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).as("a session cookie").isNotNull().isNotEmpty();
        String session = cookies.get(cookies.size() - 1);
        return session.substring(0, session.indexOf(';'));
    }

    private static String tokenIn(String body) {
        Matcher token = Pattern.compile("name=\"_csrf\" value=\"([^\"]*)\"").matcher(body);
        assertThat(token.find()).as("a hidden token in the page").isTrue();
        return token.group(1);
    }
}
