package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;

import com.example.skillora_platform.course.entity.ResourceType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonResourceResponse {

    private Long id;
    private Long lessonId;
    private String name;
    private String fileUrl;
    private ResourceType resourceType;
    private Long sizeBytes;
    private int orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
