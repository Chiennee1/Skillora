package com.example.skillora_platform.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.quiz.entity.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestionIdOrderByOrderIndexAscIdAsc(Long questionId);
}
