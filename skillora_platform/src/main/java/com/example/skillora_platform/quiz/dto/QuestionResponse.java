package com.example.skillora_platform.quiz.dto;

import java.util.List;

import com.example.skillora_platform.quiz.entity.QuestionType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {

    private Long id;
    private String content;
    private QuestionType type;
    private int points;
    private int orderIndex;
    private String explanation;
    private List<AnswerOptionResponse> answerOptions;
}
