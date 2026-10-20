package com.example.api.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class RegistrationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void registeringSignsYouStraightIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/v1/users").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada Lovelace", "username": "api_reg_user", "password": "correct horse"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("api_reg_user"))
                .andExpect(jsonPath("$.admin").value(false));

        mvc.perform(get("/api/v1/users/me").session(session))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("api_reg_user"));
    }

    @Test
    void aTakenUsernameComesBackAsAConflict() throws Exception {
        mvc.perform(post("/api/v1/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "First", "username": "api_reg_taken", "password": "correct horse"}
                        """));

        mvc.perform(post("/api/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Second", "username": "api_reg_taken", "password": "correct horse"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Username already taken"))
                .andExpect(jsonPath("$.detail").value("That username is taken."));
    }

    @Test
    void aShortPasswordFailsValidation() throws Exception {
        mvc.perform(post("/api/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada", "username": "api_reg_short", "password": "seven"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request content."));
    }

    @Test
    void aPostWithoutATokenIsRejected() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada", "username": "api_reg_nocsrf", "password": "correct horse"}
                                """))
                .andExpect(status().isForbidden());
    }
}
