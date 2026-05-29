package com.example.skillora_platform.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.quiz.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByLessonId(Long lessonId);

    boolean existsByLessonIdAndIdNot(Long lessonId, Long id);

    Optional<Quiz> findByLessonId(Long lessonId);

    @Query("""
            SELECT q FROM Quiz q
            JOIN FETCH q.lesson l
            JOIN FETCH l.section s
            JOIN FETCH s.course c
            JOIN FETCH c.instructor
            WHERE q.id = :id
            """)
    Optional<Quiz> findByIdWithLesson(@Param("id") Long id);
}
