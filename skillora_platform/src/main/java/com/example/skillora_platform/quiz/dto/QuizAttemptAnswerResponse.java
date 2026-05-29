package com.example.skillora_platform.quiz.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizAttemptAnswerResponse {

    private Long questionId;
    private Boolean correct;
    private BigDecimal pointsEarned;
    private List<Long> selectedOptionIds;
    private String textAnswer;
}
