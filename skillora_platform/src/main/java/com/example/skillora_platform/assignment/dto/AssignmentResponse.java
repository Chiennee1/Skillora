package com.example.skillora_platform.assignment.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentResponse {

    private Long id;
    private Long lessonId;
    private Long courseId;
    private String lessonTitle;
    private String title;
    private String instructions;
    private int maxScore;
    private Integer dueDays;
    private LocalDateTime dueAt;
    private boolean overdue;
    private AssignmentSubmissionResponse mySubmission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
