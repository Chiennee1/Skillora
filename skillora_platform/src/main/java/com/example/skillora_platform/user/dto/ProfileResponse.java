package com.example.skillora_platform.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String headline;
    private String bio;
    private String website;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
