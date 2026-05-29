package com.example.skillora_platform.assignment.dto;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentSubmitRequest {

    private String content;

    @Size(max = 1000, message = "File URL must be at most 1000 characters")
    private String fileUrl;
}
