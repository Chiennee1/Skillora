package com.example.skillora_platform.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.quiz.entity.QuizAttemptAnswerOption;
import com.example.skillora_platform.quiz.entity.QuizAttemptAnswerOptionId;

public interface QuizAttemptAnswerOptionRepository
        extends JpaRepository<QuizAttemptAnswerOption, QuizAttemptAnswerOptionId> {
}
