package com.example.skillora_platform.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.entity.CourseStats;
import com.example.skillora_platform.admin.repository.CourseStatsRepository;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseStatsService {

    private final CourseStatsRepository courseStatsRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Recalculates and persists all stats for a specific course from source tables.
     */
    @Transactional
    public void refreshStats(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        int totalEnrollments = course.getTotalEnrollments();
        int totalCompletions = (int) enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.COMPLETED);
        int totalReviews = (int) reviewRepository.countPublishedByCourseId(courseId);
        BigDecimal avgRating = reviewRepository.avgRatingByCourseId(courseId)
                .map(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
        int totalLessons = course.getTotalLessons();
        int totalDurationSeconds = course.getTotalDurationSeconds();

        CourseStats stats = courseStatsRepository.findById(courseId)
                .orElseGet(() -> {
                    CourseStats newStats = new CourseStats();
                    newStats.setCourse(course);
                    newStats.setCourseId(courseId);
                    newStats.setTotalRevenue(BigDecimal.ZERO);
                    return newStats;
                });

        stats.setTotalEnrollments(totalEnrollments);
        stats.setTotalCompletions(totalCompletions);
        stats.setTotalReviews(totalReviews);
        stats.setAvgRating(avgRating);
        stats.setTotalLessons(totalLessons);
        stats.setTotalDurationSeconds(totalDurationSeconds);

        courseStatsRepository.save(stats);
        log.debug("Refreshed course stats for courseId={}", courseId);
    }
}
