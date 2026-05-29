package com.example.skillora_platform.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.quiz.entity.QuizAttemptAnswer;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {
}
