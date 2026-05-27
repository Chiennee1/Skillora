package com.example.skillora_platform.user.dto;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 1000, message = "Avatar URL must be at most 1000 characters")
    private String avatarUrl;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 200, message = "Headline must be at most 200 characters")
    private String headline;

    private String bio;

    @Size(max = 255, message = "Website must be at most 255 characters")
    private String website;

    @Size(max = 150, message = "Location must be at most 150 characters")
    private String location;
}
