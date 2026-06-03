package com.example.skillora_platform.enrollment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    Page<Enrollment> findByUserIdAndStatusOrderByEnrolledAtDesc(
            Long userId, EnrollmentStatus status, Pageable pageable);

    Page<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, EnrollmentStatus status);

    long countByUserId(Long userId);

    long countByStatus(EnrollmentStatus status);

    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = :courseId AND e.status = :status")
    Optional<Enrollment> findByUserIdAndCourseIdAndStatus(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status);
}
