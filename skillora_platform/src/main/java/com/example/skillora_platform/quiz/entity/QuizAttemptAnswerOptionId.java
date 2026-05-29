package com.example.skillora_platform.quiz.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuizAttemptAnswerOptionId implements Serializable {

    @Column(name = "attempt_answer_id")
    private Long attemptAnswerId;

    @Column(name = "option_id")
    private Long optionId;
}
