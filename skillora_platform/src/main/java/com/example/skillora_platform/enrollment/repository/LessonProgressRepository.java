package com.example.skillora_platform.enrollment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.enrollment.entity.LessonProgress;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);

    List<LessonProgress> findByEnrollmentIdOrderByLessonIdAsc(Long enrollmentId);

    long countByEnrollmentIdAndCompletedTrue(Long enrollmentId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.enrollment.id = :enrollmentId")
    long countByEnrollmentId(@Param("enrollmentId") Long enrollmentId);
}
