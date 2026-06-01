package com.example.skillora_platform.review.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseRatingService {

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Recalculates and updates avg_rating and total_reviews on the courses table.
     * Called after review create, update, or delete.
     */
    @Transactional
    public void refreshCourseRating(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        long totalReviews = reviewRepository.countPublishedByCourseId(courseId);
        double avgRating = reviewRepository.avgRatingByCourseId(courseId).orElse(0.0);

        course.setTotalReviews((int) totalReviews);
        course.setAvgRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        courseRepository.save(course);

        log.debug("Refreshed course {} rating: avg={}, total={}", courseId, avgRating, totalReviews);
    }
}
