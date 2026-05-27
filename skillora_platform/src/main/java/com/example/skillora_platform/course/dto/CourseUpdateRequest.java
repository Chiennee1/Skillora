package com.example.skillora_platform.course.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.skillora_platform.course.entity.CourseLevel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 500, message = "Subtitle must be at most 500 characters")
    private String subtitle;

    private String description;

    @Size(max = 1000, message = "Thumbnail URL must be at most 1000 characters")
    private String thumbnailUrl;

    @Size(max = 1000, message = "Preview video URL must be at most 1000 characters")
    private String previewVideoUrl;

    private CourseLevel level;

    @Size(max = 20, message = "Language must be at most 20 characters")
    private String language;

    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount price must be non-negative")
    private BigDecimal discountPrice;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    private List<Integer> categoryIds;
    private List<@Size(max = 500, message = "Requirement must be at most 500 characters") String> requirements;
    private List<@Size(max = 500, message = "Outcome must be at most 500 characters") String> outcomes;
}
