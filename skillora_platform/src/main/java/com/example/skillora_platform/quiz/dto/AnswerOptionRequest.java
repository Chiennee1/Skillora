package com.example.skillora_platform.quiz.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOptionRequest {

    @NotBlank(message = "Answer option content is required")
    private String content;

    private Boolean correct;

    private Integer orderIndex;
}
