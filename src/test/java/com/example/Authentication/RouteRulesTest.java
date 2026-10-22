package com.example.Authentication;

import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Who may reach what.
 *
 * <p>The default the explicit filter chain replaces is {@code anyRequest().authenticated()}, so
 * the thing worth a test is not that the private routes are private -- it is that all the public
 * ones stayed public.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteRulesTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    /** The health check the host calls is "/", and anything but a 2xx there is a dead deploy. */
    @Test
    void theLandingPageAnswersAnAnonymousRequest() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/menu", "/loginMenu", "/createUser", "/questions",
            "/play", "/api/v1/questions/export", "/css/base.css", "/js/theme.js"})
    void everyPublicPageIsServedSignedOut(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/guest"})
    void theGuestAndPlayEntryPointsRedirectRatherThanChallenge(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().is3xxRedirection());
    }

    @Test
    void aGuestPlaysAWholeRoundWithoutSigningIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/guest").session(session)).andExpect(redirectedUrl("/menu"));
        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("player", "Guest"))
                .andExpect(model().attribute("authenticated", false));

        mvc.perform(post("/play").with(csrf()).session(session)).andExpect(redirectedUrl("/play/question"));
        int total = (Integer) mvc.perform(get("/play/question").session(session)).andReturn()
                .getModelAndView().getModel().get("totalQuestions");
        for (int position = 1; position <= total; position++) {
            var page = mvc.perform(get("/play/question").session(session)).andExpect(status().isOk()).andReturn();
            var question = (com.example.Game.Question) page.getModelAndView().getModel().get("question");
            mvc.perform(post("/play/answer").session(session).with(csrf())
                    .param("roundId", Integer.toString(((com.example.Controller.PlayController.ActiveRound) session.getAttribute("activeRound")).id()))
                    .param("position", Integer.toString(position)).param("answer", question.getAnswer()))
                    .andExpect(redirectedUrl("/play/question"));
        }
        mvc.perform(get("/play/end").session(session)).andExpect(status().isOk());
    }

    @Test
    void theScoresPageAsksAGuestToSignInFirst() throws Exception {
        mvc.perform(get("/stats"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/loginMenu"));
    }

    @Test
    void importIsClosedToAGuest() throws Exception {
        mvc.perform(post("/questions/import").with(csrf()))
                .andExpect(redirectedUrl("http://localhost/loginMenu"));
    }

    @Test
    void importIsClosedToASignedInPlayerWhoIsNotAnAdministrator() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada").param("username", "rules_plain").param("password", "correct horse"));

        mvc.perform(post("/questions/import").session(session).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void theQuestionsPageOffersImportOnlyToAnAdministrator() throws Exception {
        MockHttpSession plain = new MockHttpSession();
        mvc.perform(post("/createUser").session(plain).with(csrf())
                .param("name", "Ada").param("username", "rules_page").param("password", "correct horse"));
        mvc.perform(get("/menu").session(plain))
                .andExpect(model().attribute("authenticated", true));
        mvc.perform(get("/questions").session(plain))
                .andExpect(model().attribute("admin", false));

        users.promoteToAdmin("rules_page");
        MockHttpSession admin = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(admin).with(csrf())
                .param("username", "rules_page").param("password", "correct horse"));
        mvc.perform(get("/questions").session(admin))
                .andExpect(model().attribute("admin", true));
    }

    @Test
    void aFormPostWithNoTokenIsRefused() throws Exception {
        mvc.perform(post("/createUser")
                        .param("name", "Ada").param("username", "rules_notoken")
                        .param("password", "correct horse"))
                .andExpect(status().isForbidden());
    }

    @Test
    void answeringWithNoTokenIsRefused() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/play").with(csrf()).session(session));
        mvc.perform(get("/play/question").session(session));

        mvc.perform(post("/play/answer").session(session).param("answer", "no idea"))
                .andExpect(status().isForbidden());
    }

    @Test
    void signingInWithNoTokenIsRefused() throws Exception {
        mvc.perform(post("/loginServlet")
                        .param("username", "rules_notoken").param("password", "correct horse"))
                .andExpect(status().isForbidden());
    }
}
