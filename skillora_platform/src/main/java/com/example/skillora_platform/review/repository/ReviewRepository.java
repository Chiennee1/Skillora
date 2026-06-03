package com.example.skillora_platform.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.review.entity.Review;
import com.example.skillora_platform.review.entity.ReviewStatus;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByEnrollmentId(Long enrollmentId);

    @Query("SELECT r FROM Review r WHERE r.course.id = :courseId AND r.status = :status ORDER BY r.createdAt DESC")
    Page<Review> findByCourseIdAndStatus(
            @Param("courseId") Long courseId,
            @Param("status") ReviewStatus status,
            Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId AND r.status = 'PUBLISHED'")
    Optional<Double> avgRatingByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.status = 'PUBLISHED'")
    long countPublishedByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = 'PUBLISHED'")
    long countAllPublished();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.status = 'PUBLISHED'")
    Optional<Double> avgRatingAllPublished();
}
