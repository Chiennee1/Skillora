package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.skillora_platform.course.entity.LessonType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonResponse {

    private Long id;
    private Long sectionId;
    private Long courseId;
    private String title;
    private String slug;
    private LessonType type;
    private String content;
    private int durationSeconds;
    private boolean preview;
    private boolean published;
    private int orderIndex;
    private LessonVideoResponse video;
    private List<LessonResourceResponse> resources;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
