package com.example.skillora_platform.enrollment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private String courseSlug;
    private String courseThumbnailUrl;
    private String courseLevel;
    private String instructorName;
    private EnrollmentStatus status;
    private BigDecimal amountPaid;
    private BigDecimal progressPercent;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
}
