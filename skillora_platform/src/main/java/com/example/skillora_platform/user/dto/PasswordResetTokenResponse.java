package com.example.skillora_platform.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PasswordResetTokenResponse {

    private String resetToken;
    private LocalDateTime expiresAt;
}
