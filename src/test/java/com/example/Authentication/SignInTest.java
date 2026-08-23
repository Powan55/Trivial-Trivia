package com.example.Authentication;

import com.example.Controller.LoginController;
import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The sign-in path, now that the security filter chain owns it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignInTest {

    /** Raw BCrypt of "correct horse", written by the file datastore long before this milestone. */
    private static final String LEGACY_HASH = "$2a$10$jApN1zYYVxlVAgHbqfZEOuKLm2kDtC09Baej4iQVWi.GLpJw/zE0q";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * The trap this milestone had to walk around: stored hashes carry no encoder prefix, so
     * Spring's default DelegatingPasswordEncoder would have rejected every account at once.
     */
    @Test
    void aHashWrittenBeforeSpringSecurityStillSignsIn() throws Exception {
        users.insert("Legacy Account", "sec_legacy", LEGACY_HASH);

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "sec_legacy").param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("player", "Legacy Account (sec_legacy)"))
                .andExpect(model().attribute("authenticated", true));
    }

    @Test
    void theStoredHashIsWhatTheConfiguredEncoderMatches() {
        assertThat(encoder.matches("correct horse", LEGACY_HASH)).isTrue();
        assertThat(encoder.encode("correct horse")).doesNotStartWith("{");
    }

    /** Its absence is what makes Boot print a generated password to stdout on every boot. */
    @Test
    void aRealUserDetailsServiceIsConfigured() {
        assertThat(userDetailsService).isInstanceOf(PlayerDetailsService.class);
    }

    @Test
    void theSessionIdChangesOnSignIn() throws Exception {
        register("sec_fixation");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/loginMenu").session(session));
        String before = session.getId();

        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "sec_fixation").param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        assertThat(session.getId()).isNotEqualTo(before);
        mvc.perform(get("/menu").session(session)).andExpect(model().attribute("authenticated", true));
    }

    @Test
    void registeringChangesTheSessionIdToo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/createUser").session(session));
        String before = session.getId();

        mvc.perform(post("/createUser").session(session).with(csrf())
                        .param("name", "Ada").param("username", "sec_regfix")
                        .param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        assertThat(session.getId()).isNotEqualTo(before);
    }

    @Test
    void aWrongPasswordComesBackToTheSignInPageWithOneMessage() throws Exception {
        register("sec_wrong");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "sec_wrong").param("password", "not it"))
                .andExpect(redirectedUrl("/loginMenu?error"));

        mvc.perform(get("/loginMenu").session(session).param("error", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", LoginController.REJECTED));
    }

    @Test
    void anUnknownUserIsToldExactlyWhatABadPasswordIs() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "sec_nobody").param("password", "correct horse"))
                .andExpect(redirectedUrl("/loginMenu?error"));

        mvc.perform(get("/loginMenu").session(session).param("error", ""))
                .andExpect(model().attribute("error", LoginController.REJECTED));
    }

    @Test
    void thePageSaysNothingAboutAnErrorThatWasNotAskedAbout() throws Exception {
        mvc.perform(get("/loginMenu"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
    }

    /** Five wrong guesses and the right password stops working, which is the point. */
    @Test
    void tooManyFailuresLockTheAccountOut() throws Exception {
        register("sec_throttled");

        for (int attempt = 0; attempt < LoginAttemptService.MAX_FAILURES; attempt++) {
            mvc.perform(post("/loginServlet").with(csrf())
                    .param("username", "sec_throttled").param("password", "not it"));
        }

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "sec_throttled").param("password", "correct horse"))
                .andExpect(redirectedUrl("/loginMenu?error"));

        mvc.perform(get("/loginMenu").session(session).param("error", ""))
                .andExpect(model().attribute("error", LoginController.LOCKED_OUT));
        mvc.perform(get("/menu").session(session)).andExpect(model().attribute("authenticated", false));
    }

    @Test
    void aGetCannotSignAnyoneOut() throws Exception {
        MockHttpSession session = register("sec_getlogout");

        mvc.perform(get("/logout").session(session));

        assertThat(session.isInvalid()).isFalse();
        mvc.perform(get("/menu").session(session)).andExpect(model().attribute("authenticated", true));
    }

    @Test
    void aPostSignsYouOut() throws Exception {
        MockHttpSession session = register("sec_postlogout");

        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/loginMenu"));

        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/menu").session(new MockHttpSession()))
                .andExpect(model().attribute("authenticated", false));
    }

    @Test
    void signingOutWithoutATokenIsRefused() throws Exception {
        MockHttpSession session = register("sec_logoutcsrf");

        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());

        assertThat(session.isInvalid()).isFalse();
    }

    private MockHttpSession register(String username) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                        .param("name", "Ada Lovelace").param("username", username)
                        .param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));
        return session;
    }
}
