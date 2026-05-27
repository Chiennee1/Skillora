package com.example.skillora_platform.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.video.bunny")
public record BunnyStreamProperties(
        String libraryId,
        String apiKey,
        String tusEndpoint,
        Duration uploadExpiration
) {

    private static final String DEFAULT_TUS_ENDPOINT = "https://video.bunnycdn.com/tusupload";
    private static final Duration DEFAULT_UPLOAD_EXPIRATION = Duration.ofHours(24);

    public String resolvedTusEndpoint() {
        if (tusEndpoint == null || tusEndpoint.isBlank()) {
            return DEFAULT_TUS_ENDPOINT;
        }
        return tusEndpoint;
    }

    public Duration resolvedUploadExpiration() {
        if (uploadExpiration == null) {
            return DEFAULT_UPLOAD_EXPIRATION;
        }
        return uploadExpiration;
    }
}
