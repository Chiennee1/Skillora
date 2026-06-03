package com.example.skillora_platform.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
    private boolean emailVerified;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
