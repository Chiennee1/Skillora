package com.example.skillora_platform.assignment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    boolean existsByLessonId(Long lessonId);

    Optional<Assignment> findByLessonId(Long lessonId);

    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.lesson l
            JOIN FETCH l.section s
            JOIN FETCH s.course c
            JOIN FETCH c.instructor
            WHERE a.id = :id
            """)
    Optional<Assignment> findByIdWithLesson(@Param("id") Long id);
}
