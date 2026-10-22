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
        mvc.perform(post("/play").with(csrf()).session(session)).andExpect(redirectedUrl("/play/question"));
        int id = ((PlayController.ActiveRound) session.getAttribute("activeRound")).id();
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/play/question").session(session)).andExpect(status().isOk())
                    .andExpect(model().attribute("roundId", id)).andExpect(model().attribute("position", 1));
        }
        mvc.perform(get("/play/end").session(session)).andExpect(redirectedUrl("/play/question"));
    }

    @Test
    void missingForgedStaleAndUnservedIdentifiersCannotAnswer() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(post("/play").with(csrf()).session(session));
        int stale = ((PlayController.ActiveRound) session.getAttribute("activeRound")).id();
        mvc.perform(post("/play").with(csrf()).session(session));
        int active = ((PlayController.ActiveRound) session.getAttribute("activeRound")).id();
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
        mvc.perform(post("/play").with(csrf()).session(alice));
        mvc.perform(post("/play").with(csrf()).session(bob));
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
                ((PlayController.ActiveRound) alice.getAttribute("activeRound")).ownerToken(), null).score()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(rounds.summary((Integer) b.get("roundId"),
                ((PlayController.ActiveRound) bob.getAttribute("activeRound")).ownerToken(), null).score()).isZero();
    }
    @Test
    void concurrentStartsPublishMatchingRoundAndOwner() throws Exception {
        var firstPublished = new java.util.concurrent.CountDownLatch(1);
        var secondFinished = new java.util.concurrent.CountDownLatch(1);
        var firstThread = new java.util.concurrent.atomic.AtomicReference<Thread>();
        var session = new MockHttpSession() {
            @Override
            public void setAttribute(String name, Object value) {
                super.setAttribute(name, value);
                if (Thread.currentThread() == firstThread.get()
                        && (name.equals("activeRound") || name.equals("roundOwner"))) {
                    firstPublished.countDown();
                    try {
                        if (!secondFinished.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
                            throw new AssertionError("Second start did not finish");
                        }
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(interrupted);
                    }
                }
            }
        };
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> {
                firstThread.set(Thread.currentThread());
                return mvc.perform(post("/play").with(csrf()).session(session)).andExpect(redirectedUrl("/play/question"));
            });
            var second = executor.submit(() -> {
                if (!firstPublished.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    throw new AssertionError("First start did not publish");
                }
                try {
                    return mvc.perform(post("/play").with(csrf()).session(session)).andExpect(redirectedUrl("/play/question"));
                } finally {
                    secondFinished.countDown();
                }
            });
            first.get(15, java.util.concurrent.TimeUnit.SECONDS);
            second.get(15, java.util.concurrent.TimeUnit.SECONDS);
            var active = (PlayController.ActiveRound) session.getAttribute("activeRound");
            mvc.perform(get("/play/question").session(session)).andExpect(status().isOk())
                    .andExpect(model().attribute("roundId", active.id()))
                    .andExpect(model().attribute("position", 1));
            org.assertj.core.api.Assertions.assertThat(rounds.summary(active.id(), active.ownerToken(), null).total())
                    .isPositive();
        } finally {
            secondFinished.countDown();
            executor.shutdownNow();
        }
    }
}
