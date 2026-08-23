package com.example.Controller;

import com.example.Game.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Register, sign out, sign back in, play a round, read it back off the scores page.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void aRegisteredPlayerCanSignBackIn() throws Exception {
        register("flow_one", "Ada Lovelace");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "flow_one").param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("player", "Ada Lovelace (flow_one)"));
    }

    @Test
    void signingOutAndBackInWorks() throws Exception {
        MockHttpSession session = register("flow_again", "Ada Lovelace");
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/loginMenu"));

        MockHttpSession second = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(second).with(csrf())
                        .param("username", "flow_again").param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));
        mvc.perform(get("/menu").session(second))
                .andExpect(model().attribute("authenticated", true));
    }

    @Test
    void theWrongPasswordGoesBackToTheSignInPageWithAMessage() throws Exception {
        register("flow_two", "Ada Lovelace");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                        .param("username", "flow_two").param("password", "wrong password"))
                .andExpect(redirectedUrl("/loginMenu?error"));

        mvc.perform(get("/loginMenu").session(session).param("error", ""))
                .andExpect(model().attribute("error", LoginController.REJECTED));
        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("authenticated", false));
    }

    @Test
    void anUnknownUserGetsTheSameMessageAsABadPassword() throws Exception {
        MvcResult unknown = mvc.perform(post("/loginServlet").with(csrf())
                .param("username", "nobody_here").param("password", "correct horse")).andReturn();

        register("flow_three", "Ada Lovelace");
        MvcResult badPassword = mvc.perform(post("/loginServlet").with(csrf())
                .param("username", "flow_three").param("password", "wrong password")).andReturn();

        assertThat(unknown.getResponse().getRedirectedUrl())
                .isEqualTo(badPassword.getResponse().getRedirectedUrl());
    }

    @Test
    void aFinishedRoundShowsUpOnTheScoresPage() throws Exception {
        MockHttpSession session = register("flow_score", "Ada Lovelace");

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).with(csrf()).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session)).andExpect(model().attribute("score", 10));

        mvc.perform(get("/stats").session(session))
                .andExpect(model().attribute("best", 10))
                .andExpect(model().attribute("scores", org.hamcrest.Matchers.hasItem(10)));
    }

    @Test
    void refreshingTheEndPageDoesNotRecordTheRoundTwice() throws Exception {
        MockHttpSession session = register("flow_refresh", "Ada Lovelace");

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).with(csrf()).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session));
        mvc.perform(get("/play/end").session(session));
        mvc.perform(get("/play/end").session(session));

        MvcResult stats = mvc.perform(get("/stats").session(session)).andReturn();
        assertThat((java.util.List<?>) stats.getModelAndView().getModel().get("scores")).hasSize(1);
    }

    @Test
    void refreshingTheEndPageDropsTheVerdictAndStillRecordsOnce() throws Exception {
        MockHttpSession session = register("flow_verdict", "Ada Lovelace");

        mvc.perform(get("/play").session(session));
        while (question(session) != null) {
            mvc.perform(post("/play/answer").session(session).with(csrf()).param("answer", "definitely wrong"));
        }

        mvc.perform(get("/play/end").session(session))
                .andExpect(model().attribute("answerCorrect", false))
                .andExpect(model().attributeExists("correctAnswer"));
        mvc.perform(get("/play/end").session(session))
                .andExpect(model().attributeDoesNotExist("answerCorrect", "correctAnswer"));

        MvcResult stats = mvc.perform(get("/stats").session(session)).andReturn();
        assertThat((java.util.List<?>) stats.getModelAndView().getModel().get("scores")).hasSize(1);
    }

    @Test
    void aGuestRoundIsNotStoredAndTheScoresPageAsksThemToSignIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/guest").session(session));

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).with(csrf()).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session)).andExpect(model().attribute("authenticated", false));

        mvc.perform(get("/stats").session(session))
                .andExpect(redirectedUrl("http://localhost/loginMenu"));
    }

    private MockHttpSession register(String username, String name) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                        .param("name", name).param("username", username).param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));
        return session;
    }

    private Question question(MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get("/play/question").session(session)).andReturn();
        return result.getModelAndView() == null
                ? null
                : (Question) result.getModelAndView().getModel().get("question");
    }
}
