package com.example.skillora_platform.user.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.skillora_platform.user.entity.UserStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private UserStatus status;
    private boolean emailVerified;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
