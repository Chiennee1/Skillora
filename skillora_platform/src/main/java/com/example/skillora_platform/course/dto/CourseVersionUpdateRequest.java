package com.example.skillora_platform.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseVersionUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 500, message = "Subtitle must be at most 500 characters")
    private String subtitle;

    private String description;

    @Size(max = 1000, message = "Thumbnail URL must be at most 1000 characters")
    private String thumbnailUrl;
}
