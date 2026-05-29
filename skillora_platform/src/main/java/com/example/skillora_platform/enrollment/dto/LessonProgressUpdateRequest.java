package com.example.skillora_platform.enrollment.dto;

import jakarta.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonProgressUpdateRequest {

    @Min(value = 0, message = "Watched seconds must be >= 0")
    private Integer watchedSeconds;

    private Boolean completed;
}
