package com.example.Controller;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.Persistence.QuestionRepository;
import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The pages, through a real servlet container.
 *
 * <p>MockMvc records a forward instead of performing one, so it cannot see what happens when the
 * view resolver forwards to {@code /WEB-INF/views/*.jsp}. Boot runs the security filter on every
 * dispatcher type by default, forwards included, and that forward matches no rule -- so every
 * page in the app answered 302 to the sign-in page, and so did the sign-in page. Nothing in the
 * MockMvc suite could have caught it.</p>
 *
 * <p>Every assertion here names the page it expects by title. The client follows redirects, so a
 * status code and a closing tag are also what a redirect to the sign-in page looks like: a
 * regression that made one page private would land on a perfectly valid {@code /loginMenu} and
 * pass. The title is the only thing in the response that says which view actually rendered.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RenderedPagesTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserRepository users;

    @Autowired
    private QuestionRepository questions;

    @Autowired
    private JdbcClient jdbc;

    @LocalServerPort
    private int port;

    /**
     * The autowired client follows redirects, which is what the page assertions above want. The
     * sign-in flow needs the opposite: a followed redirect swallows the Set-Cookie carrying the
     * session id that changed at sign-in, and every request after it goes out unauthenticated.
     */
    private final TestRestTemplate direct = notFollowingRedirects();

    private static TestRestTemplate notFollowingRedirects() {
        HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();
        TestRestTemplate template = new TestRestTemplate();
        template.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory(client));
        return template;
    }

    /** Every page a signed-out visitor can reach, and the title that proves it is that page. */
    static Stream<Arguments> publicPages() {
        return Stream.of(
                Arguments.of("/", "Trivial Trivia"),
                Arguments.of("/menu", "Main menu - Trivial Trivia"),
                Arguments.of("/loginMenu", "Sign in - Trivial Trivia"),
                Arguments.of("/createUser", "Create an account - Trivial Trivia"),
                Arguments.of("/questions", "Questions - Trivial Trivia"),
                Arguments.of("/play", "Choose a round - Trivial Trivia"),
                Arguments.of("/play/question", "Choose a round - Trivial Trivia"),
                Arguments.of("/play/end", "Choose a round - Trivial Trivia"));
    }

    /** The host's health check is a plain GET of "/". Anything but 2xx is a deploy that never goes live. */
    @Test
    void theLandingPageIsTwoHundredToAnAnonymousVisitor() {
        ResponseEntity<String> response = rest.getForEntity("/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<title>Trivial Trivia</title>");
    }

    @ParameterizedTest
    @MethodSource("publicPages")
    void everyPublicPageRendersSignedOut(String path, String title) {
        ResponseEntity<String> response = rendered(path);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<title>" + title + "</title>").contains("</html>");
    }

    @Test
    void aGuestCanStartAndSeeAQuestion() {
        Map<String, String> jar = new LinkedHashMap<>();
        startGuestRound(jar);
        ResponseEntity<String> question = get("/play/question", cookieHeader(jar));

        assertThat(question.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(question.getBody()).contains("Submit answer");
    }

    @Test
    void aRenderedRoundKeepsItsPositionOnRefreshAndShowsResultsOnlyWhenFinished() {
        int total = Math.min(5, questions.count());
        Map<String, String> jar = new LinkedHashMap<>();
        ResponseEntity<String> missing = get("/play/end", null);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(missing.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/play");

        startGuestRound(jar);
        ResponseEntity<String> page = get("/play/question", cookieHeader(jar));
        collectCookies(jar, page);
        String roundId = hiddenIn(page.getBody(), "roundId");
        assertThat(roundId).matches("[1-9][0-9]*");
        String firstQuestion = page.getBody().substring(page.getBody().indexOf("<h1 id=\"question-heading\""),
                page.getBody().indexOf("</h1>") + 5);

        ResponseEntity<String> early = get("/play/end", cookieHeader(jar));
        assertThat(early.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(early.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/play/question");
        ResponseEntity<String> refreshed = get("/play/question", cookieHeader(jar));
        collectCookies(jar, refreshed);
        assertThat(refreshed.getBody()).contains(firstQuestion);
        assertThat(hiddenIn(refreshed.getBody(), "roundId")).isEqualTo(roundId);
        assertThat(hiddenIn(refreshed.getBody(), "position")).isEqualTo("1");

        int answered = 0;
        page = refreshed;
        while (page.getStatusCode() == HttpStatus.OK) {
            assertThat(++answered).isLessThanOrEqualTo(total);
            String body = page.getBody();
            assertThat(hiddenIn(body, "roundId")).isEqualTo(roundId);
            assertThat(hiddenIn(body, "position")).isEqualTo(Integer.toString(answered));
            Matcher option = Pattern.compile("name=\"answer\"\\s+value=\"([^\"]*)\"").matcher(body);
            assertThat(option.find()).isTrue();
            ResponseEntity<String> submitted = post("/play/answer", cookieHeader(jar),
                    field("roundId", roundId), field("position", hiddenIn(body, "position")),
                    field("answer", org.springframework.web.util.HtmlUtils.htmlUnescape(option.group(1))),
                    field("_csrf", tokenIn(body)));
            assertThat(submitted.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            collectCookies(jar, submitted);
            page = get("/play/question", cookieHeader(jar));
            collectCookies(jar, page);
        }
        assertThat(answered).isEqualTo(total);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(page.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/play/end");
        ResponseEntity<String> result = get("/play/end", cookieHeader(jar));
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).contains("<title>Game over - Trivial Trivia</title>");
        assertThat(get("/play/end", cookieHeader(jar)).getBody()).contains("<title>Game over - Trivial Trivia</title>");
    }

    /** Export is open to anyone, and it is the one public route that renders no view at all. */
    @Test
    void theQuestionSetExportsToAnyone() {
        ResponseEntity<String> export = rest.getForEntity("/api/v1/questions/export?format=csv", String.class);

        assertThat(export.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(export.getBody()).startsWith("\"Question\",\"Option1\"");
        assertThat(export.getHeaders().getFirst("Content-Disposition")).contains("questions.csv");
        assertThat(export.getHeaders().getFirst("Content-Security-Policy")).isNotNull();
    }

    /** The three pages that show a form to a signed-out visitor. The rest have nothing to post. */
    static Stream<Arguments> pagesWithForms() {
        return Stream.of(
                Arguments.of("/loginMenu", "Sign in - Trivial Trivia"),
                Arguments.of("/createUser", "Create an account - Trivial Trivia"),
                Arguments.of("/play", "Choose a round - Trivial Trivia"));
    }

    /** The token the forms need is read straight out of the request attribute, with no taglib. */
    @ParameterizedTest
    @MethodSource("pagesWithForms")
    void everyRenderedFormCarriesARealCsrfToken(String path, String title) {
        String body = rendered(path).getBody();

        assertThat(body).contains("<title>" + title + "</title>");
        assertThat(tokenIn(body)).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("publicPages")
    void everyPageCarriesAContentSecurityPolicy(String path, String title) {
        ResponseEntity<String> page = rendered(path);

        assertThat(page.getBody()).contains("<title>" + title + "</title>");
        assertThat(policyOf(page)).contains("default-src 'self'")
                .contains("object-src 'none'")
                .contains("base-uri 'self'")
                .contains("form-action 'self'")
                .contains("frame-ancestors 'none'")
                .doesNotContain("unsafe-eval");
    }

    /**
     * The failure this guards is silent: a nonce that does not match blocks the pre-paint theme
     * script and the page still renders, just wrong, and only a browser console says so.
     */
    @ParameterizedTest
    @MethodSource("publicPages")
    void theInlineScriptCarriesTheNonceTheHeaderAllows(String path, String title) {
        ResponseEntity<String> page = rendered(path);

        assertThat(page.getBody()).contains("<title>" + title + "</title>");
        Matcher onTheTag = Pattern.compile("<script nonce=\"([^\"]+)\"").matcher(page.getBody());

        assertThat(onTheTag.find()).as("a nonced inline script on %s", path).isTrue();
        assertThat(onTheTag.group(1)).isEqualTo(nonceIn(policyOf(page)));
    }

    @Test
    void inlineScriptIsAllowedByNonceRatherThanByBeingInline() {
        String policy = policyOf(rest.getForEntity("/", String.class));

        assertThat(policy).contains("script-src 'self' 'nonce-");
        assertThat(policy.substring(policy.indexOf("script-src"), policy.indexOf("style-src")))
                .doesNotContain("unsafe-inline");
    }

    @Test
    void twoResponsesDoNotShareANonce() {
        assertThat(nonceIn(policyOf(rest.getForEntity("/", String.class))))
                .isNotEqualTo(nonceIn(policyOf(rest.getForEntity("/", String.class))));
    }

    @Test
    void theScoresPageStillAsksAGuestToSignIn() {
        assertThat(rest.getForEntity("/stats", String.class).getBody())
                .contains("<title>Sign in - Trivial Trivia</title>")
                .doesNotContain("Best score");
    }

    @Test
    void historyRendersSavedDatesCountsAndLegacyGapsThroughTheContainer() {
        String username = "render_history";
        String cookie = signInAsPlayer(username);
        jdbc.sql("update users set display_name = ? where username = ?")
                .params("<script>History</script>", username).update();
        cookie = signIn(username);
        ResponseEntity<String> empty = get("/stats", cookie);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).contains("<title>Your scores - Trivial Trivia</title>")
                .contains("No finished rounds yet").contains("href=\"/play\"");

        jdbc.sql("""
                insert into rounds (user_id, score, correct_count, wrong_count, total_questions, finished_at)
                select id, 37, 3, 2, 5, timestamp '2026-10-22 19:08:30' from users where username = ?
                """).param(username).update();
        jdbc.sql("""
                insert into rounds (user_id, score, correct_count, wrong_count, total_questions, finished_at)
                select id, 0, 0, 5, 5, timestamp '2026-10-20 09:08:30' from users where username = ?
                """).param(username).update();
        jdbc.sql("""
                insert into rounds (user_id, score, finished_at)
                select id, 90, timestamp '2026-10-30 09:08:30' from users where username = ?
                """).param(username).update();

        String page = get("/stats", cookie).getBody();
        assertThat(page).contains("<title>Your scores - Trivial Trivia</title>")
                .contains("22 Oct 2026, 19:08").contains("datetime=\"2026-10-22T19:08:30\"")
                .contains("37 points").contains("3 / 5 correct").contains("0 / 5 correct")
                .contains("Date unavailable").contains("Answer counts unavailable")
                .contains("Dates shown in server time.").contains("Newest first")
                .contains("&lt;script&gt;History&lt;/script&gt;").doesNotContain("<script>History</script>")
                .doesNotContain("30 Oct 2026").doesNotContain("0 / 0");
        assertThat(page.indexOf("22 Oct 2026, 19:08")).isLessThan(page.indexOf("20 Oct 2026, 09:08"));
        assertThat(page.indexOf("20 Oct 2026, 09:08")).isLessThan(page.indexOf("Date unavailable"));
    }

    /**
     * The import token travels inside a multipart body, and whether that reaches CsrfFilter is a
     * container question, not a Spring one: the filter runs before the DispatcherServlet, so it
     * only sees the token if Tomcat parses the parts on getParameter. MockMvc's csrf() attaches
     * the token as an ordinary parameter and never exercises any of that.
     */
    @Test
    void anAdministratorImportsWithTheTokenInsideTheMultipartBody() {
        List<String[]> shipped = questions.export();
        try {
            String cookie = signInAsAdmin("render_admin");
            // CsrfAuthenticationStrategy deletes the CSRF cookie on sign-in and regenerates it
            // "upon the next request" (its own javadoc) rather than on the sign-in response
            // itself, so this GET is what actually produces the cookie the import below needs.
            ResponseEntity<String> questionsPage = get("/questions", cookie);
            cookie = withUpdatedCookies(cookie, questionsPage);
            String page = questionsPage.getBody();
            assertThat(page).contains("<title>Questions - Trivial Trivia</title>")
                    .contains("<label for=\"file\">");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", upload("q.csv", """
                    Question,Option1,Option2,Option3,Option4,Answer
                    Largest ocean?,Atlantic,Indian,Pacific,Arctic,Pacific
                    """));
            body.add("_csrf", tokenIn(page));

            ResponseEntity<String> imported = direct.exchange(url("/questions/import"),
                    HttpMethod.POST, new HttpEntity<>(body, multipart(cookie)), String.class);

            assertThat(imported.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(imported.getBody()).contains("Question set replaced.");
            assertThat(questions.count()).isEqualTo(1);
        } finally {
            questions.replaceAll(shipped);
        }
    }

    @Test
    void theSameImportWithoutTheTokenIsRefused() {
        List<String[]> shipped = questions.export();
        try {
            String cookie = signInAsAdmin("render_notoken");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", upload("q.csv", "Question,Option1,Option2,Option3,Option4,Answer\n"
                    + "Largest ocean?,Atlantic,Indian,Pacific,Arctic,Pacific\n"));

            ResponseEntity<String> refused = direct.exchange(url("/questions/import"),
                    HttpMethod.POST, new HttpEntity<>(body, multipart(cookie)), String.class);

            assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(questions.export()).usingRecursiveComparison().isEqualTo(shipped);
        } finally {
            questions.replaceAll(shipped);
        }
    }

    /**
     * Who gets the import form, asserted on the page instead of on a model attribute no view
     * reads.
     *
     * <p>The sign-out form is the observable for "this session is signed in": the header renders
     * it only when there is a principal. Asserting it on the non-admin session is the half that
     * matters -- without it, "sees no import form" passes for a guest too, and the test would say
     * nothing about whether being signed in is what is being refused.</p>
     */
    @Test
    void onlyAnAdministratorSeesTheImportForm() {
        String player = signInAsPlayer("render_form_player");
        String admin = signInAsAdmin("render_form_admin");

        String asGuest = rest.getForEntity("/questions", String.class).getBody();
        String asPlayer = get("/questions", player).getBody();
        String asAdmin = get("/questions", admin).getBody();

        // Signed in, or not, according to the page itself.
        assertThat(asGuest).doesNotContain(SIGN_OUT_FORM).contains("/loginMenu");
        assertThat(asPlayer).contains(SIGN_OUT_FORM);
        assertThat(asAdmin).contains(SIGN_OUT_FORM);

        // And only one of the three is offered the form that replaces everyone's questions.
        assertThat(asGuest).doesNotContain(IMPORT_FORM);
        assertThat(asPlayer).doesNotContain(IMPORT_FORM).contains("needs an administrator account");
        assertThat(asAdmin).contains(IMPORT_FORM).doesNotContain("needs an administrator account");
    }

    /**
     * The other tests in this class prove sign-out only by asserting the form's markup is
     * present, never by submitting it. This is the real round trip: the session that answers
     * `/stats` before this POST must not answer it after.
     */
    @Test
    void signingOutEndsTheSessionForReal() {
        String cookie = signInAsPlayer("render_signout");

        ResponseEntity<String> menu = get("/menu", cookie);
        cookie = withUpdatedCookies(cookie, menu);
        assertThat(menu.getBody()).contains(SIGN_OUT_FORM);

        ResponseEntity<String> signedOut = post("/logout", cookie, field("_csrf", tokenIn(menu.getBody())));
        assertThat(signedOut.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(signedOut.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/loginMenu");

        // `direct` doesn't follow redirects (needed above, to read the Location header itself),
        // so the actual "am I still signed in" check needs `rest`, which does.
        cookie = withUpdatedCookies(cookie, signedOut);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        ResponseEntity<String> stats = rest.exchange("/stats", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertThat(stats.getBody()).contains("<title>Sign in - Trivial Trivia</title>");
    }

    /** Rendered only when the request carries a principal, so it doubles as "am I signed in". */
    private static final String SIGN_OUT_FORM = "action=\"/logout\" method=\"post\"";
    private static final String IMPORT_FORM = "action=\"/questions/import\"";

    private String signInAsPlayer(String username) {
        register(username);
        return signIn(username);
    }

    /** Register, promote in the database, sign in: the whole way an administrator comes to exist. */
    private String signInAsAdmin(String username) {
        register(username);
        assertThat(users.promoteToAdmin(username)).isTrue();
        // Signed in after the promotion, because the authority is read off the row at sign-in.
        return signIn(username);
    }

    private void register(String username) {
        Map<String, String> jar = new LinkedHashMap<>();
        ResponseEntity<String> form = get("/createUser", null);
        collectCookies(jar, form);
        ResponseEntity<String> registered = post("/createUser", cookieHeader(jar),
                field("name", "Ada Lovelace"), field("username", username),
                field("password", "correct horse"), field("_csrf", tokenIn(form.getBody())));

        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(registered.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/menu");
    }

    private String signIn(String username) {
        // Two cookies now carry the round trip: JSESSIONID for the session, XSRF-TOKEN for the
        // CSRF repository. A browser sends every cookie it holds, not just the newest one, so the
        // jar has to be merged rather than replaced.
        Map<String, String> jar = new LinkedHashMap<>();
        ResponseEntity<String> page = get("/loginMenu", null);
        collectCookies(jar, page);
        ResponseEntity<String> signedIn = post("/loginServlet", cookieHeader(jar),
                field("username", username), field("password", "correct horse"),
                field("_csrf", tokenIn(page.getBody())));

        assertThat(signedIn.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(signedIn.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/menu");
        // Session fixation rotates JSESSIONID and CsrfAuthenticationStrategy rotates the CSRF
        // token on the same request; merging keeps whichever of the two changed and whichever
        // didn't.
        collectCookies(jar, signedIn);
        return cookieHeader(jar);
    }

    @SafeVarargs
    private ResponseEntity<String> post(String path, String cookie, Map.Entry<String, String>... fields) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> field : fields) {
            form.add(field.getKey(), field.getValue());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (cookie != null) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return direct.exchange(url(path), HttpMethod.POST, new HttpEntity<>(form, headers), String.class);
    }

    private ResponseEntity<String> get(String path, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        if (cookie != null) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return direct.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private void startGuestRound(Map<String, String> jar) {
        ResponseEntity<String> setup = get("/play", cookieHeader(jar));
        collectCookies(jar, setup);
        assertThat(setup.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(setup.getBody()).contains("<title>Choose a round - Trivial Trivia</title>");
        ResponseEntity<String> started = post("/play", cookieHeader(jar),
                field("category", hiddenIn(setup.getBody(), "category")),
                field("difficulty", hiddenIn(setup.getBody(), "difficulty")),
                field("_csrf", tokenIn(setup.getBody())));
        assertThat(started.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(started.getHeaders().getFirst(HttpHeaders.LOCATION)).endsWith("/play/question");
        collectCookies(jar, started);
    }

    private ResponseEntity<String> rendered(String path) {
        Map<String, String> jar = new LinkedHashMap<>();
        for (int redirects = 0; redirects < 5; redirects++) {
            ResponseEntity<String> page = get(path, cookieHeader(jar));
            collectCookies(jar, page);
            if (!page.getStatusCode().is3xxRedirection()) {
                return page;
            }
            path = page.getHeaders().getLocation().getPath();
        }
        throw new AssertionError("Too many redirects rendering " + path);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static HttpHeaders multipart(String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HttpHeaders.COOKIE, cookie);
        return headers;
    }

    private static ByteArrayResource upload(String filename, String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private static Map.Entry<String, String> field(String name, String value) {
        return Map.entry(name, value);
    }

    private static void collectCookies(Map<String, String> jar, ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            return;
        }
        for (String cookie : cookies) {
            String pair = cookie.substring(0, cookie.indexOf(';'));
            String name = pair.substring(0, pair.indexOf('='));
            String value = pair.substring(pair.indexOf('=') + 1);
            // CsrfAuthenticationStrategy deletes the CSRF cookie on sign-in (empty value,
            // Max-Age=0) rather than rotating it in place. A real browser drops a deleted
            // cookie instead of resending it, so the jar has to as well.
            if (value.isEmpty()) {
                jar.remove(name);
            } else {
                jar.put(name, value);
            }
        }
    }

    private static String withUpdatedCookies(String cookie, ResponseEntity<?> response) {
        Map<String, String> jar = new LinkedHashMap<>();
        if (cookie != null) {
            for (String pair : cookie.split("; ")) {
                jar.put(pair.substring(0, pair.indexOf('=')), pair.substring(pair.indexOf('=') + 1));
            }
        }
        collectCookies(jar, response);
        return cookieHeader(jar);
    }

    private static String cookieHeader(Map<String, String> jar) {
        if (jar.isEmpty()) {
            return null;
        }
        return jar.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
    }

    private static String tokenIn(String body) {
        return hiddenIn(body, "_csrf");
    }

    private static String hiddenIn(String body, String name) {
        Matcher token = Pattern.compile("name=\"" + Pattern.quote(name) + "\" value=\"([^\"]*)\"").matcher(body);
        assertThat(token.find()).as("hidden %s in the page", name).isTrue();
        return token.group(1);
    }

    private static String policyOf(ResponseEntity<String> response) {
        String policy = response.getHeaders().getFirst("Content-Security-Policy");
        assertThat(policy).as("Content-Security-Policy header").isNotNull();
        return policy;
    }

    private static String nonceIn(String policy) {
        Matcher nonce = Pattern.compile("'nonce-([^']+)'").matcher(policy);
        assertThat(nonce.find()).as("a nonce in %s", policy).isTrue();
        return nonce.group(1);
    }
}
