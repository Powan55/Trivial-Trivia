package com.example.Controller;

import java.util.List;

import com.example.Persistence.QuestionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlaySetupTest {
    @Autowired MockMvc mvc;
    @Autowired QuestionRepository questions;
    @Autowired JdbcClient jdbc;
    private List<String[]> original;

    @BeforeEach
    void remember() { original = questions.export(); }

    @AfterEach
    void restore() { questions.replaceAll(original); }

    @Test
    void previewDoesNotStartOrReplaceActiveRound() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(post("/play").session(session).with(csrf())).andExpect(redirectedUrl("/play/question"));
        Object active = session.getAttribute("activeRound");
        int count = jdbc.sql("select count(*) from rounds").query(Integer.class).single();
        mvc.perform(get("/play").session(session)).andExpect(status().isOk()).andExpect(view().name("playSetup"))
                .andExpect(model().attribute("matchingCount", original.size() - 1))
                .andExpect(model().attribute("roundSize", Math.min(5, original.size() - 1)))
                .andExpect(model().attribute("selectedCategory", ""))
                .andExpect(model().attribute("selectedDifficulty", ""));
        mvc.perform(get("/play").session(session).param("category", "Geography").param("difficulty", "EASY"))
                .andExpect(model().attribute("matchingCount", 1)).andExpect(model().attribute("roundSize", 1));
        assertThat(session.getAttribute("activeRound")).isSameAs(active);
        assertThat(jdbc.sql("select count(*) from rounds").query(Integer.class).single()).isEqualTo(count);
    }

    @Test
    void invalidZeroAndStaleStartsKeepCurrentRoundAndBankUntouched() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(post("/play").session(session).with(csrf())).andExpect(redirectedUrl("/play/question"));
        Object active = session.getAttribute("activeRound");
        int count = jdbc.sql("select count(*) from rounds").query(Integer.class).single();
        mvc.perform(post("/play").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/play").session(session).with(csrf()).param("category", "Missing"))
                .andExpect(status().isBadRequest()).andExpect(view().name("playSetup"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("selectedCategory", ""))
                .andExpect(model().attribute("selectedDifficulty", ""))
                .andExpect(model().attribute("matchingCount", original.size() - 1));
        mvc.perform(post("/play").session(session).with(csrf()).param("difficulty", "impossible"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/play").session(session).with(csrf()).param("category", "Geography").param("difficulty", "HARD"))
                .andExpect(status().isConflict()).andExpect(view().name("playSetup"))
                .andExpect(model().attribute("selectedCategory", "Geography"))
                .andExpect(model().attribute("selectedDifficulty", "HARD"))
                .andExpect(model().attribute("matchingCount", 0));
        questions.replaceAll(List.<String[]>of(QuestionRepository.HEADER));
        mvc.perform(post("/play").session(session).with(csrf()).param("category", "Geography"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/play").session(session).with(csrf())).andExpect(status().isConflict());
        assertThat(session.getAttribute("activeRound")).isSameAs(active);
        assertThat(jdbc.sql("select count(*) from rounds").query(Integer.class).single()).isEqualTo(count);
    }
}
