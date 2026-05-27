package com.example.skillora_platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.skillora_platform.user.entity.RoleName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    private RoleName role;

    @Size(max = 200, message = "Instructor title must be at most 200 characters")
    private String instructorTitle;

    @Size(max = 500, message = "Instructor expertise must be at most 500 characters")
    private String instructorExpertise;
}
