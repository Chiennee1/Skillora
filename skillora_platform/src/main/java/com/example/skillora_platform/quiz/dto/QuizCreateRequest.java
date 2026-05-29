package com.example.skillora_platform.quiz.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizCreateRequest {

    @NotNull(message = "Lesson id is required")
    private Long lessonId;

    @NotBlank(message = "Quiz title is required")
    @Size(max = 255, message = "Quiz title must be at most 255 characters")
    private String title;

    private String description;

    @Min(value = 0, message = "Pass score must be at least 0")
    @Max(value = 100, message = "Pass score must be at most 100")
    private Integer passScore;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMins;

    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts;

    private Boolean shuffleQuestions;

    @Valid
    @NotEmpty(message = "At least one question is required")
    private List<QuestionRequest> questions;
}
