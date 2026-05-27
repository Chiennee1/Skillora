package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SectionResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private int orderIndex;
    private boolean published;
    private List<LessonSummaryResponse> lessons;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
