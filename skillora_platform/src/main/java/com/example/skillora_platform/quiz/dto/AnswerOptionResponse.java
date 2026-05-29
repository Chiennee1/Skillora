package com.example.skillora_platform.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerOptionResponse {

    private Long id;
    private String content;
    private Boolean correct;
    private int orderIndex;
}
