package com.example.Controller;

import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Test
    void registeringSignsYouStraightIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                        .param("name", "Ada Lovelace")
                        .param("username", "ada_reg")
                        .param("password", "correct horse"))
                .andExpect(redirectedUrl("/menu"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("player", "Ada Lovelace (ada_reg)"));
    }

    /** Registration hands out ROLE_USER and nothing more: admin is not a thing you can sign up as. */
    @Test
    void registeringDoesNotMakeYouAnAdministrator() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada Lovelace").param("username", "ada_role").param("password", "correct horse"));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("authenticated", true));
        mvc.perform(get("/questions").session(session))
                .andExpect(model().attribute("admin", false));
    }

    @Test
    void theStoredRowNeverContainsThePassword() throws Exception {
        mvc.perform(post("/createUser").with(csrf())
                .param("name", "Grace Hopper")
                .param("username", "grace_reg")
                .param("password", "correct horse"));

        assertThat(users.findByUsername("grace_reg"))
                .hasValueSatisfying(stored -> assertThat(stored.passwordHash()).doesNotContain("correct horse"));
    }

    @Test
    void aTakenUsernameComesBackToTheFormWithAMessage() throws Exception {
        mvc.perform(post("/createUser").with(csrf())
                .param("name", "First").param("username", "taken_reg").param("password", "correct horse"));

        mvc.perform(post("/createUser").with(csrf())
                        .param("name", "Second").param("username", "taken_reg").param("password", "correct horse"))
                .andExpect(status().isOk())
                .andExpect(view().name("createUser"))
                .andExpect(model().attribute("error", "That username is taken."))
                .andExpect(model().attribute("username", "taken_reg"));
    }

    @Test
    void aShortPasswordIsRejectedEvenThoughTheFormSaysMinlength() throws Exception {
        mvc.perform(post("/createUser").with(csrf())
                        .param("name", "Ada").param("username", "short_reg").param("password", "seven"))
                .andExpect(view().name("createUser"))
                .andExpect(model().attributeExists("error"));

        assertThat(users.findByUsername("short_reg")).isEmpty();
    }

    @Test
    void aBadUsernameIsRejected() throws Exception {
        mvc.perform(post("/createUser").with(csrf())
                        .param("name", "Ada").param("username", "ada lovelace").param("password", "correct horse"))
                .andExpect(view().name("createUser"))
                .andExpect(model().attributeExists("error"));
    }

    /**
     * The guest path no longer clears whoever was signed in. A GET that ends a session is the
     * vulnerability class this milestone took out of /logout, and it does not get to live here.
     */
    @Test
    void theGuestPathIsOpenToAnyoneAndEndsNobodysSession() throws Exception {
        mvc.perform(get("/guest").session(new MockHttpSession())).andExpect(redirectedUrl("/menu"));

        MockHttpSession signedIn = new MockHttpSession();
        mvc.perform(post("/createUser").session(signedIn).with(csrf())
                .param("name", "Ada").param("username", "guest_reg").param("password", "correct horse"));

        mvc.perform(get("/guest").session(signedIn)).andExpect(redirectedUrl("/menu"));

        assertThat(signedIn.isInvalid()).isFalse();
        mvc.perform(get("/menu").session(signedIn))
                .andExpect(model().attribute("authenticated", true));
    }

    @Test
    void aGuestSessionPlaysAsAGuest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/guest").session(session));

        mvc.perform(get("/menu").session(session))
                .andExpect(model().attribute("authenticated", false))
                .andExpect(model().attribute("player", "Guest"));
    }

    @Test
    void signingOutEndsTheSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada").param("username", "logout_reg").param("password", "correct horse"));

        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/loginMenu"));
        assertThat(session.isInvalid()).isTrue();

        mvc.perform(get("/menu").session(new MockHttpSession()))
                .andExpect(model().attribute("authenticated", false));
    }
}
