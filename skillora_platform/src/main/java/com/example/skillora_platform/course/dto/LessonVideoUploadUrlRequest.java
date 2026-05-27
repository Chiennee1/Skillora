package com.example.skillora_platform.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonVideoUploadUrlRequest {

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must be at most 255 characters")
    private String fileName;

    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type must be at most 100 characters")
    private String mimeType;

    @Positive(message = "File size must be positive")
    private Long fileSizeBytes;
}
