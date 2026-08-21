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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlayControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void startingARoundGoesToTheFirstQuestion() throws Exception {
        mvc.perform(get("/play").session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/play/question"));
    }

    @Test
    void aQuestionRendersTheQuestionPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/play").session(session));

        mvc.perform(get("/play/question").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("question"))
                .andExpect(forwardedUrl("/WEB-INF/views/questionPage.jsp"));
    }

    @Test
    void runningOutOfQuestionsGoesToTheEndPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/play").session(session));

        while (question(session) != null) {
            mvc.perform(post("/play/answer").session(session).param("answer", "no idea"));
        }

        mvc.perform(get("/play/question").session(session))
                .andExpect(redirectedUrl("/play/end"));
    }

    @Test
    void theEndPageReportsTheScoreAndTheTally() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/play").session(session));

        Question first = question(session);
        mvc.perform(post("/play/answer").session(session).param("answer", first.getAnswer()));
        Question second = question(session);
        mvc.perform(post("/play/answer").session(session).param("answer", "definitely wrong"));

        assertThat(second).isNotNull();
        mvc.perform(get("/play/end").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("score", 10))
                .andExpect(model().attribute("right", 1))
                .andExpect(model().attribute("wrong", 1))
                .andExpect(forwardedUrl("/WEB-INF/views/endGamePage.jsp"));
    }

    /**
     * Two players at once. GameService used to be a singleton, so the second player's answers
     * landed on the first player's score and each of them skipped the other's questions.
     */
    @Test
    void twoSessionsKeepSeparateScores() throws Exception {
        MockHttpSession alice = new MockHttpSession();
        MockHttpSession bob = new MockHttpSession();
        mvc.perform(get("/play").session(alice));
        mvc.perform(get("/play").session(bob));

        Question aliceSees = question(alice);
        Question bobSees = question(bob);
        assertThat(bobSees.getQuestion()).isEqualTo(aliceSees.getQuestion());

        mvc.perform(post("/play/answer").session(alice).param("answer", aliceSees.getAnswer()));
        mvc.perform(post("/play/answer").session(bob).param("answer", "definitely wrong"));

        mvc.perform(get("/play/end").session(alice)).andExpect(model().attribute("score", 10));
        mvc.perform(get("/play/end").session(bob)).andExpect(model().attribute("score", 0));
    }

    /** Fetches the next question by driving the controller, or null once the round is over. */
    private Question question(MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get("/play/question").session(session)).andReturn();
        if (result.getModelAndView() == null) {
            return null;
        }
        return (Question) result.getModelAndView().getModel().get("question");
    }
}
