package com.example.skillora_platform.admin.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.example.skillora_platform.course.entity.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStats {

    @Id
    @Column(name = "course_id")
    private Long courseId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "total_enrollments", nullable = false)
    private int totalEnrollments;

    @Column(name = "total_reviews", nullable = false)
    private int totalReviews;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_completions", nullable = false)
    private int totalCompletions;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_lessons", nullable = false)
    private int totalLessons;

    @Column(name = "total_duration_seconds", nullable = false)
    private int totalDurationSeconds;

    @Column(name = "last_enrolled_at")
    private LocalDateTime lastEnrolledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
