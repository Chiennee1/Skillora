package com.example.skillora_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.oauth2")
public record AppOAuth2Properties(
        String authorizedRedirectUri
) {
}
