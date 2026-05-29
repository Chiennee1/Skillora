package com.example.skillora_platform.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.course.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findByIdAndDeletedAtIsNull(Long id);

    List<Lesson> findBySectionIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(Long sectionId);

    List<Lesson> findBySectionCourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsBySectionIdAndOrderIndex(Long sectionId, int orderIndex);

    boolean existsBySectionIdAndOrderIndexAndIdNot(Long sectionId, int orderIndex, Long id);

    boolean existsBySectionIdAndSlugAndDeletedAtIsNull(Long sectionId, String slug);

    boolean existsBySectionIdAndSlugAndDeletedAtIsNullAndIdNot(Long sectionId, String slug, Long id);

    @Query("""
            SELECT COUNT(l) FROM Lesson l
            WHERE l.section.course.id = :courseId
            AND l.deletedAt IS NULL
            AND l.section.deletedAt IS NULL
            """)
    long countActiveLessonsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT COALESCE(SUM(l.durationSeconds), 0) FROM Lesson l
            WHERE l.section.course.id = :courseId
            AND l.deletedAt IS NULL
            AND l.section.deletedAt IS NULL
            """)
    long sumActiveDurationSecondsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT l FROM Lesson l
            JOIN FETCH l.section s
            WHERE s.course.id = :courseId
            AND l.deletedAt IS NULL
            AND l.published = true
            AND s.deletedAt IS NULL
            AND s.published = true
            ORDER BY s.orderIndex ASC, l.orderIndex ASC
            """)
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);
}
