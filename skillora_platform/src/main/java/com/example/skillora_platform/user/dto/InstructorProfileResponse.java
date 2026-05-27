package com.example.skillora_platform.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InstructorProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private String title;
    private String expertise;
    private String introVideoUrl;
    private boolean verified;
    private String headline;
    private String bio;
    private String website;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
