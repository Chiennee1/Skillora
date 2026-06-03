package com.example.skillora_platform.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminCourseResponse {

    private Long id;
    private String title;
    private String slug;
    private String status;
    private String level;
    private BigDecimal price;
    private String currency;
    private String rejectReason;
    private int totalLessons;
    private int totalEnrollments;
    private BigDecimal avgRating;
    private int totalReviews;
    private String instructorName;
    private String instructorEmail;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
