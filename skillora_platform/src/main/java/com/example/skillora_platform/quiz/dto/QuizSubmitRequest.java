package com.example.skillora_platform.quiz.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {

    @Valid
    @NotEmpty(message = "At least one answer is required")
    private List<QuizAnswerSubmitRequest> answers;
}
