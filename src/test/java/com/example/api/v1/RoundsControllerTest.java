package com.example.api.v1;

import com.example.Persistence.RoundRepository;
import com.example.Service.RoundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoundsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RoundRepository rounds;

    @Autowired
    private RoundService roundService;

    @Autowired
    private JdbcClient jdbc;

    /** The bug the entry point/access-denied wiring exists to fix: no silent 302 to /loginMenu. */
    @Test
    void aSignedOutRequestGetsA401ProblemBodyNotARedirect() throws Exception {
        mvc.perform(get("/api/v1/rounds"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void aSignedInPlayerGetsTheirScoreHistory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada Lovelace").param("username", "api_rounds_user")
                .param("password", "correct horse"));

        rounds.record("api_rounds_user", 40, 4, 6);
        rounds.record("api_rounds_user", 90, 9, 1);

        mvc.perform(get("/api/v1/rounds").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores", contains(40, 90)))
                .andExpect(jsonPath("$.best").value(90));
    }

    @Test
    void onlyCompletedRoundsAppearOnceInTheirOwnersHistory() throws Exception {
        String username = "api_active_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada Lovelace").param("username", username)
                .param("password", "correct horse"))
                .andExpect(status().is3xxRedirection());

        String ownerToken = "api_history_owner";
        int roundId = roundService.start(ownerToken, username, null, null);
        mvc.perform(get("/api/v1/rounds").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores").isEmpty())
                .andExpect(jsonPath("$.rounds").isEmpty())
                .andExpect(jsonPath("$.best").value(0));

        var page = roundService.serve(roundId, ownerToken, username);
        while (page.position() < page.total()) {
            roundService.answer(roundId, page.position(), page.question().getAnswer(), ownerToken, username);
            page = roundService.serve(roundId, ownerToken, username);
        }
        mvc.perform(get("/api/v1/rounds").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores").isEmpty())
                .andExpect(jsonPath("$.best").value(0));

        int expectedScore = page.total() * 10;
        roundService.answer(roundId, page.position(), page.question().getAnswer(), ownerToken, username);
        mvc.perform(get("/api/v1/rounds").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$.rounds", hasSize(1)))
                .andExpect(jsonPath("$.rounds[0].correctCount").value(page.total()))
                .andExpect(jsonPath("$.rounds[0].totalQuestions").value(page.total()))
                .andExpect(jsonPath("$.scores", contains(expectedScore)))
                .andExpect(jsonPath("$.best").value(expectedScore));

        roundService.answer(roundId, page.position(), page.question().getAnswer(), ownerToken, username);
        mvc.perform(get("/api/v1/rounds").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores", contains(expectedScore)))
                .andExpect(jsonPath("$.rounds", hasSize(1)))
                .andExpect(jsonPath("$.best").value(expectedScore));

        MockHttpSession otherSession = new MockHttpSession();
        String otherUsername = "api_other_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(post("/createUser").session(otherSession).with(csrf())
                .param("name", "Grace Hopper").param("username", otherUsername)
                .param("password", "correct horse"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/v1/rounds").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores").isEmpty())
                .andExpect(jsonPath("$.rounds").isEmpty())
                .andExpect(jsonPath("$.best").value(0));
    }

    @Test
    void detailedHistoryIsAdditiveAndKeepsLocalDatesAndExplicitNulls() throws Exception {
        String username = "api_history_details";
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "History").param("username", username).param("password", "correct horse"))
                .andExpect(status().is3xxRedirection());
        rounds.record(username, 37, 3, 2);
        rounds.record(username, 0, 0, 5);
        rounds.record(username, 90, 0, 0);
        rounds.record(username, 0, 0, 0);
        jdbc.sql("""
                update rounds set finished_at = timestamp '2026-10-22 19:08:30',
                total_questions = case when correct_count + wrong_count > 0 then 5 else 0 end
                where user_id = (select id from users where username = ?)
                """).param(username).update();
        jdbc.sql("""
                update rounds set finished_at = timestamp '2026-10-20 09:08:30'
                where user_id = (select id from users where username = ?) and score = 0 and wrong_count = 5
                """).param(username).update();

        String body = mvc.perform(get("/api/v1/rounds").session(session)
                        .param("username", "api_rounds_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores", contains(37, 0, 90, 0)))
                .andExpect(jsonPath("$.best").value(90))
                .andExpect(jsonPath("$.rounds", hasSize(4)))
                .andExpect(jsonPath("$.rounds[0].score").value(37))
                .andExpect(jsonPath("$.rounds[0].finishedAt").value("2026-10-22T19:08:30"))
                .andExpect(jsonPath("$.rounds[0].correctCount").value(3))
                .andExpect(jsonPath("$.rounds[0].totalQuestions").value(5))
                .andExpect(jsonPath("$.rounds[1].correctCount").value(0))
                .andExpect(jsonPath("$.rounds[1].totalQuestions").value(5))
                .andExpect(jsonPath("$.rounds[2].score").value(0))
                .andExpect(jsonPath("$.rounds[3].score").value(90))
                .andReturn().getResponse().getContentAsString();
        var entries = new ObjectMapper().readTree(body).path("rounds");
        for (int index = 2; index < 4; index++) {
            var entry = entries.get(index);
            org.assertj.core.api.Assertions.assertThat(entry.size()).isEqualTo(4);
            for (String field : java.util.List.of("finishedAt", "correctCount", "totalQuestions")) {
                org.assertj.core.api.Assertions.assertThat(entry.has(field)).isTrue();
                org.assertj.core.api.Assertions.assertThat(entry.get(field).isNull()).isTrue();
            }
        }
    }
}
