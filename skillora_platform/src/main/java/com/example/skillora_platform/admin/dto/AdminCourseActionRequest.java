package com.example.skillora_platform.admin.dto;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCourseActionRequest {

    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;
}
