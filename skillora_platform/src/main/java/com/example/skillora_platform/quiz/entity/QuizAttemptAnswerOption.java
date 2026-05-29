package com.example.skillora_platform.quiz.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_attempt_answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptAnswerOption {

    @EmbeddedId
    private QuizAttemptAnswerOptionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("attemptAnswerId")
    @JoinColumn(name = "attempt_answer_id", nullable = false)
    private QuizAttemptAnswer attemptAnswer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("optionId")
    @JoinColumn(name = "option_id", nullable = false)
    private AnswerOption option;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
