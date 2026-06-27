package com.example.skillora_platform.config;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "skillora.security.jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "JWT issuer is required")
        String issuer,

        @NotBlank(message = "JWT secret is required")
        @Size(min = 32, message = "JWT secret must be at least 32 characters")
        String secret,

        @NotNull(message = "JWT access token TTL is required")
        Duration accessTokenTtl,

        @NotNull(message = "JWT refresh token TTL is required")
        Duration refreshTokenTtl
) {
}
