package com.example.api.v1;

import com.example.Persistence.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestionsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private QuestionRepository questions;

    @Test
    void listsEveryQuestionWithNoAnswerField() throws Exception {
        int count = questions.count();

        mvc.perform(get("/api/v1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(count))
                .andExpect(jsonPath("$[0].text").exists())
                .andExpect(jsonPath("$[0].option1").exists())
                .andExpect(jsonPath("$[0].answer").doesNotExist());
    }
}
