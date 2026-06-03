package com.example.skillora_platform.admin.dto;

import jakarta.validation.constraints.NotNull;

import com.example.skillora_platform.user.entity.UserStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
