package com.example.skillora_platform.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must be at most 120 characters")
    private String name;

    private Integer parentId;

    @Size(max = 1000, message = "Icon URL must be at most 1000 characters")
    private String iconUrl;

    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;
}
