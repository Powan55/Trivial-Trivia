package com.example.api.v1;

import java.util.List;

import com.example.Persistence.QuestionRepository;
import com.example.api.v1.dto.QuestionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiV1QuestionsController")
public class QuestionsController {

    private final QuestionRepository questions;

    public QuestionsController(QuestionRepository questions) {
        this.questions = questions;
    }

    @GetMapping("/api/v1/questions")
    public List<QuestionDto> allQuestions() {
        return questions.findAll().stream().map(QuestionDto::from).toList();
    }
}
