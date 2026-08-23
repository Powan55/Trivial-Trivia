package com.example.Controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.example.Persistence.QuestionRepository;
import com.example.Persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionsControllerTest {

    private static final String HEADER = "Question,Option1,Option2,Option3,Option4,Answer\n";
    private static final String ONE_ROW = "Largest ocean?,Atlantic,Indian,Pacific,Arctic,Pacific\n";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private QuestionRepository questions;

    @Autowired
    private UserRepository users;

    private List<String[]> original;

    @BeforeEach
    void rememberTheQuestionSet() {
        original = questions.export();
    }

    @AfterEach
    void putTheQuestionSetBack() {
        questions.replaceAll(original);
    }

    /** Register, promote in the database, then sign in: the whole way an administrator exists. */
    private MockHttpSession admin(String username) throws Exception {
        mvc.perform(post("/createUser").with(csrf())
                .param("name", "Ada Lovelace").param("username", username).param("password", "correct horse"));
        assertThat(users.promoteToAdmin(username)).isTrue();

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginServlet").session(session).with(csrf())
                .param("username", username).param("password", "correct horse"));
        return session;
    }

    private MockHttpSession signedInPlayer(String username) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/createUser").session(session).with(csrf())
                .param("name", "Ada Lovelace").param("username", username).param("password", "correct horse"));
        return session;
    }

    /** String[] rows compare by identity, so this has to go element by element. */
    private void assertTheQuestionSetIsUntouched() {
        assertThat(questions.export())
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    private static MockMultipartFile upload(String filename, String body) {
        return new MockMultipartFile("file", filename, null, body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void thePageCountsTheQuestions() throws Exception {
        mvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attribute("count", original.size() - 1))
                .andExpect(model().attribute("formats", org.hamcrest.Matchers.hasItems("csv", "json", "xml")));
    }

    @Test
    void exportComesBackAsAnAttachmentInEachFormat() throws Exception {
        mvc.perform(get("/questions/export").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("questions.csv")));

        mvc.perform(get("/questions/export").param("format", "json"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("[")));

        mvc.perform(get("/questions/export").param("format", "xml"))
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<rows>")));
    }

    @Test
    void anUnknownExportFormatIsRefused() throws Exception {
        mvc.perform(get("/questions/export").param("format", "yaml")).andExpect(status().isBadRequest());
    }

    @Test
    void anAdministratorCanReplaceTheQuestionSet() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + ONE_ROW))
                        .session(admin("import_ok")).with(csrf()))
                .andExpect(model().attribute("message", "Question set replaced."))
                .andExpect(model().attribute("count", 1));

        assertThat(questions.export()).hasSize(2);
    }

    @Test
    void jsonAndXmlImportsWorkToo() throws Exception {
        MockHttpSession session = admin("import_formats");

        String json = "[[\"Question\",\"Option1\",\"Option2\",\"Option3\",\"Option4\",\"Answer\"],"
                + "[\"Largest ocean?\",\"Atlantic\",\"Indian\",\"Pacific\",\"Arctic\",\"Pacific\"]]";
        mvc.perform(multipart("/questions/import").file(upload("q.json", json)).session(session).with(csrf()))
                .andExpect(model().attribute("count", 1));

        String xml = "<rows><row><cell>Question</cell><cell>Option1</cell><cell>Option2</cell>"
                + "<cell>Option3</cell><cell>Option4</cell><cell>Answer</cell></row>"
                + "<row><cell>Largest ocean?</cell><cell>Atlantic</cell><cell>Indian</cell>"
                + "<cell>Pacific</cell><cell>Arctic</cell><cell>Pacific</cell></row></rows>";
        mvc.perform(multipart("/questions/import").file(upload("q.xml", xml)).session(session).with(csrf()))
                .andExpect(model().attribute("count", 1));
    }

    @Test
    void aGuestIsSentToSignInRatherThanReplacingTheQuestionSet() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + ONE_ROW)).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTheQuestionSetIsUntouched();
    }

    /** Signed in is not enough: replacing the set changes it for everyone who plays. */
    @Test
    void aSignedInPlayerWhoIsNotAnAdministratorIsRefused() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + ONE_ROW))
                        .session(signedInPlayer("import_plain")).with(csrf()))
                .andExpect(status().isForbidden());

        assertTheQuestionSetIsUntouched();
    }

    @Test
    void anImportWithNoTokenIsRefused() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + ONE_ROW))
                        .session(admin("import_notoken")))
                .andExpect(status().isForbidden());

        assertTheQuestionSetIsUntouched();
    }

    @Test
    void anUnsupportedExtensionIsRefused() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.txt", HEADER + ONE_ROW))
                        .session(admin("import_ext")).with(csrf()))
                .andExpect(model().attribute("error", "Use a .csv, .json or .xml file."));

        assertTheQuestionSetIsUntouched();
    }

    @Test
    void aHeaderWithNoQuestionsIsRefused() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER))
                        .session(admin("import_empty")).with(csrf()))
                .andExpect(model().attribute("error", "That file has a header and no questions."));

        assertTheQuestionSetIsUntouched();
    }

    @Test
    void aShortRowIsRefusedAndNothingIsOverwritten() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + "Largest ocean?,Atlantic\n"))
                        .session(admin("import_short")).with(csrf()))
                .andExpect(model().attribute("error",
                        "Every row needs 6 columns: the question, four options and the answer."));

        assertTheQuestionSetIsUntouched();
    }

    @Test
    void anEmptyUploadIsRefused() throws Exception {
        mvc.perform(multipart("/questions/import").file(upload("q.csv", ""))
                        .session(admin("import_none")).with(csrf()))
                .andExpect(model().attribute("error", "Choose a file first."));
    }

    @Test
    void anImportedQuestionIsWhatTheGameThenAsks() throws Exception {
        MockHttpSession session = admin("import_play");
        mvc.perform(multipart("/questions/import").file(upload("q.csv", HEADER + ONE_ROW)).session(session).with(csrf()));

        MockHttpSession player = new MockHttpSession();
        mvc.perform(get("/play").session(player));
        mvc.perform(get("/play/question").session(player))
                .andExpect(model().attribute("question",
                        org.hamcrest.Matchers.hasToString(
                                org.hamcrest.Matchers.containsString("Largest ocean?"))));
    }
}
