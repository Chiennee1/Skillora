package com.example.skillora_platform.enrollment.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonProgressResponse {

    private Long id;
    private Long enrollmentId;
    private Long lessonId;
    private String lessonTitle;
    private int watchedSeconds;
    private int totalDurationSeconds;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
