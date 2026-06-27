package com.example.skillora_platform.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "skillora.auth")
@Validated
public record PasswordResetProperties(
        @NotBlank(message = "Password reset URL is required")
        String passwordResetUrl
) {
}
