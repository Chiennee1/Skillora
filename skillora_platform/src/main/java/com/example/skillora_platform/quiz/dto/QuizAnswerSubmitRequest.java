package com.example.skillora_platform.quiz.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerSubmitRequest {

    @NotNull(message = "Question id is required")
    private Long questionId;

    private List<Long> selectedOptionIds;

    private String textAnswer;
}
