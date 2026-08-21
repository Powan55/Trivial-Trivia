package com.example.Controller;

import com.example.Game.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Register, sign out, sign back in, play a round, read it back off the scores page.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void aRegisteredPlayerCanSignBackIn() throws Exception {
        register("flow_one", "Ada Lovelace");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session)
                        .param("username", "flow_one").param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("player", "Ada Lovelace (flow_one)"));
    }

    @Test
    void theWrongPasswordStaysOnTheSignInPageWithAMessage() throws Exception {
        register("flow_two", "Ada Lovelace");

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session)
                        .param("username", "flow_two").param("password", "wrong password"))
                .andExpect(view().name("loginmenu"))
                .andExpect(model().attributeExists("error"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("authenticated", false));
    }

    @Test
    void anUnknownUserGetsTheSameMessageAsABadPassword() throws Exception {
        MvcResult unknown = mvc.perform(post("/loginServlet")
                .param("username", "nobody_here").param("password", "correct horse")).andReturn();

        register("flow_three", "Ada Lovelace");
        MvcResult badPassword = mvc.perform(post("/loginServlet")
                .param("username", "flow_three").param("password", "wrong password")).andReturn();

        assertThat(unknown.getModelAndView().getModel().get("error"))
                .isEqualTo(badPassword.getModelAndView().getModel().get("error"));
    }

    @Test
    void aFinishedRoundShowsUpOnTheScoresPage() throws Exception {
        MockHttpSession session = register("flow_score", "Ada Lovelace");

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session)).andExpect(model().attribute("score", 10));

        mvc.perform(get("/stats").session(session))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("best", 10))
                .andExpect(model().attribute("scores", org.hamcrest.Matchers.hasItem(10)));
    }

    @Test
    void refreshingTheEndPageDoesNotRecordTheRoundTwice() throws Exception {
        MockHttpSession session = register("flow_refresh", "Ada Lovelace");

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session));
        mvc.perform(get("/play/end").session(session));
        mvc.perform(get("/play/end").session(session));

        MvcResult stats = mvc.perform(get("/stats").session(session)).andReturn();
        assertThat((java.util.List<?>) stats.getModelAndView().getModel().get("scores")).hasSize(1);
    }

    @Test
    void aGuestGetsNoScoreHistory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/guest").session(session));

        mvc.perform(get("/play").session(session));
        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).param("answer", first.getAnswer()));
        mvc.perform(get("/play/end").session(session)).andExpect(model().attribute("authenticated", false));

        mvc.perform(get("/stats").session(session))
                .andExpect(model().attribute("authenticated", false))
                .andExpect(model().attribute("scores", org.hamcrest.Matchers.empty()));
    }

    private MockHttpSession register(String username, String name) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session)
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
