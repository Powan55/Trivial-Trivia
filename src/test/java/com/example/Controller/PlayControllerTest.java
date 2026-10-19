package com.example.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayControllerTest {
    @Autowired MockMvc mvc;
    @Autowired com.example.Service.RoundService rounds;

    @Test
    void refreshKeepsQuestionAndEarlyEndRedirects() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(get("/play").session(session)).andExpect(redirectedUrl("/play/question"));
        int id = (Integer) session.getAttribute("activeRoundId");
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/play/question").session(session)).andExpect(status().isOk())
                    .andExpect(model().attribute("roundId", id)).andExpect(model().attribute("position", 1));
        }
        mvc.perform(get("/play/end").session(session)).andExpect(redirectedUrl("/play/question"));
    }

    @Test
    void missingForgedStaleAndUnservedIdentifiersCannotAnswer() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(get("/play").session(session));
        int stale = (Integer) session.getAttribute("activeRoundId");
        mvc.perform(get("/play").session(session));
        int active = (Integer) session.getAttribute("activeRoundId");
        for (int id : new int[]{stale, active, Integer.MAX_VALUE}) {
            mvc.perform(post("/play/answer").session(session).with(csrf()).param("roundId", "" + id)
                    .param("position", "1").param("answer", "Paris")).andExpect(status().isConflict());
        }
        mvc.perform(post("/play/answer").session(session).with(csrf()).param("answer", "Paris"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/play/end")).andExpect(redirectedUrl("/play"));
    }
    @Test
    void sessionsKeepQuestionsAndScoresSeparate() throws Exception {
        var alice = new MockHttpSession();
        var bob = new MockHttpSession();
        mvc.perform(get("/play").session(alice));
        mvc.perform(get("/play").session(bob));
        var a = mvc.perform(get("/play/question").session(alice)).andReturn().getModelAndView().getModel();
        var b = mvc.perform(get("/play/question").session(bob)).andReturn().getModelAndView().getModel();
        var question = (com.example.Game.Question) a.get("question");
        for (var session : new MockHttpSession[]{alice, bob}) {
            mvc.perform(post("/play/answer").session(session).with(csrf())
                    .param("roundId", a.get("roundId").toString()).param("position", "1")
                    .param("answer", question.getAnswer()))
                    .andExpect(status().is(session == alice ? 302 : 409));
            if (session == alice) {
                mvc.perform(post("/play/answer").session(session).with(csrf())
                        .param("roundId", a.get("roundId").toString()).param("position", "1")
                        .param("answer", question.getAnswer()))
                        .andExpect(flash().attribute("answerCorrect", true))
                        .andExpect(flash().attributeCount(1));
            }
        }
        mvc.perform(get("/play/question").session(alice)).andExpect(model().attribute("position", 2));
        mvc.perform(get("/play/question").session(bob)).andExpect(model().attribute("position", 1));
        org.assertj.core.api.Assertions.assertThat(a.get("roundId")).isNotEqualTo(b.get("roundId"));
        org.assertj.core.api.Assertions.assertThat(rounds.summary((Integer) a.get("roundId"),
                (String) alice.getAttribute("roundOwner"), null).score()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(rounds.summary((Integer) b.get("roundId"),
                (String) bob.getAttribute("roundOwner"), null).score()).isZero();
    }
}
