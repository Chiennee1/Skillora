package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;

import com.example.skillora_platform.course.entity.CourseVersionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseVersionResponse {
    private Long id;
    private Long courseId;
    private int versionNumber;
    private CourseVersionStatus status;
    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private String rejectReason;
    private String snapshotJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
