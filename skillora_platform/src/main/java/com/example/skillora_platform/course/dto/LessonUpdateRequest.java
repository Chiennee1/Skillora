package com.example.skillora_platform.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.skillora_platform.course.entity.LessonType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private LessonType type;
    private String content;

    @Min(value = 0, message = "Duration seconds must be non-negative")
    private Integer durationSeconds;

    private Boolean preview;
    private Boolean published;

    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;
}
