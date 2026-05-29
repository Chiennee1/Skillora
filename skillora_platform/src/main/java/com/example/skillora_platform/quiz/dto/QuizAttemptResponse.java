package com.example.skillora_platform.quiz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizAttemptResponse {

    private Long id;
    private Long quizId;
    private Long enrollmentId;
    private Long userId;
    private int attemptNo;
    private BigDecimal score;
    private boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private List<QuizAttemptAnswerResponse> answers;
}
