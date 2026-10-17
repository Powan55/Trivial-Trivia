package com.example.api.v1.dto;

import com.example.Game.Question;

public record QuestionDto(String text, String option1, String option2, String option3, String option4) {

    public static QuestionDto from(Question question) {
        return new QuestionDto(question.getQuestion(), question.getOption1(), question.getOption2(),
                question.getOption3(), question.getOption4());
    }
}
