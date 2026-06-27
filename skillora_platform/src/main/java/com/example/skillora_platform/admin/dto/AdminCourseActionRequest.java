package com.example.skillora_platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCourseActionRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;
}
