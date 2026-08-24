package com.example.api.v1;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;
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
 * A client that never loaded a JSP page -- no session, no scraped {@code ${_csrf.token}} hidden
 * field -- reading a CSRF token off the {@code XSRF-TOKEN} cookie and using it to register, the
 * way a JS SPA, Postman or anything else outside the app's own JSP session has to.
 *
 * <p>Real container, not MockMvc: {@code MockMvc}'s {@code csrf()} post-processor injects a
 * matching token directly and never exercises the cookie repository or the filter that resolves
 * the deferred token, so it cannot tell this working from this being broken.</p>
 *
 * <p>The value read off the cookie is the raw token -- {@code CookieCsrfTokenRepository} never
 * stores anything else -- so this is exactly the value that goes back in the header, with no
 * masking step this client could not perform anyway.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CsrfCookieClientTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void aCookieOnlyClientRegistersWithNoJspEverInvolved() {
        ResponseEntity<String> plainGet = rest.getForEntity("/api/v1/users/me", String.class);
        String token = xsrfTokenIn(plainGet);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, "XSRF-TOKEN=" + token);
        headers.add("X-XSRF-TOKEN", token);
        String body = """
                {"name": "Ada Lovelace", "username": "cookie_only_client", "password": "correct horse"}
                """;

        ResponseEntity<String> registered = rest.exchange("/api/v1/users", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((Boolean) JsonPath.read(registered.getBody(), "$.authenticated")).isTrue();
        assertThat((String) JsonPath.read(registered.getBody(), "$.username")).isEqualTo("cookie_only_client");
    }

    @Test
    void theSameCookieWithNoHeaderIsStillRefused() {
        ResponseEntity<String> plainGet = rest.getForEntity("/api/v1/users/me", String.class);
        String token = xsrfTokenIn(plainGet);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, "XSRF-TOKEN=" + token);
        String body = """
                {"name": "Ada Lovelace", "username": "cookie_only_no_header", "password": "correct horse"}
                """;

        ResponseEntity<String> refused = rest.exchange("/api/v1/users", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static String xsrfTokenIn(ResponseEntity<String> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).as("a Set-Cookie header on a plain API GET, with no JSP ever rendered").isNotNull();
        for (String cookie : cookies) {
            Matcher xsrf = Pattern.compile("XSRF-TOKEN=([^;]+)").matcher(cookie);
            if (xsrf.find()) {
                assertThat(cookie).as("readable by the page's own JavaScript -- HttpOnly would defeat the point")
                        .doesNotContain("HttpOnly");
                return xsrf.group(1);
            }
        }
        throw new AssertionError("no XSRF-TOKEN cookie among " + cookies);
    }
}
