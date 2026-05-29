package com.example.skillora_platform.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentCreateRequest {

    @NotNull(message = "Lesson id is required")
    private Long lessonId;

    @NotBlank(message = "Assignment title is required")
    @Size(max = 255, message = "Assignment title must be at most 255 characters")
    private String title;

    private String instructions;

    @Min(value = 1, message = "Max score must be at least 1")
    private Integer maxScore;

    @Min(value = 0, message = "Due days must be at least 0")
    private Integer dueDays;
}
