package com.example.skillora_platform.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.quiz.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    long countByEnrollmentIdAndQuizId(Long enrollmentId, Long quizId);

    List<QuizAttempt> findByEnrollmentIdAndQuizIdOrderByAttemptNoDesc(Long enrollmentId, Long quizId);
}
