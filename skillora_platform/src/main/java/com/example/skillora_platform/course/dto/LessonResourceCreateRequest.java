package com.example.skillora_platform.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.example.skillora_platform.course.entity.ResourceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonResourceCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "File URL is required")
    @Size(max = 1000, message = "File URL must be at most 1000 characters")
    private String fileUrl;

    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    @PositiveOrZero(message = "Size bytes must be non-negative")
    private Long sizeBytes;

    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;
}
