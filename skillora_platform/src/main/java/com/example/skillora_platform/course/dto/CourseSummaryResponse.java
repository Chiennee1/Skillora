package com.example.skillora_platform.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CourseSummaryResponse {

    private Long id;
    private String title;
    private String slug;
    private String subtitle;
    private String thumbnailUrl;
    private CourseLevel level;
    private String language;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String currency;
    private CourseStatus status;
    private Long instructorId;
    private String instructorName;
    private List<CategoryResponse> categories;
    private int totalLessons;
    private int totalDurationSeconds;
    private int totalEnrollments;
    private BigDecimal avgRating;
    private int totalReviews;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
