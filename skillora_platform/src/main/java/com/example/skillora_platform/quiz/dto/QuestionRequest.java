package com.example.skillora_platform.quiz.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.example.skillora_platform.quiz.entity.QuestionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequest {

    @NotBlank(message = "Question content is required")
    private String content;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @Min(value = 1, message = "Question points must be at least 1")
    private Integer points;

    private Integer orderIndex;

    private String explanation;

    @Valid
    private List<AnswerOptionRequest> answerOptions;
}
