package com.example.skillora_platform.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.quiz.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = "answerOptions")
    List<Question> findByQuizIdOrderByOrderIndexAscIdAsc(Long quizId);
}
