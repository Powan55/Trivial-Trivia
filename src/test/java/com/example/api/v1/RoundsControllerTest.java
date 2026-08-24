package com.example.api.v1;

import com.example.Persistence.RoundRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
}
