package com.example.skillora_platform.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.video.bunny")
public record BunnyStreamProperties(
        String libraryId,
        String apiKey,
        String tusEndpoint,
        Duration uploadExpiration,
        String webhookSigningSecret,
        String tokenSecurityKey,
        Boolean signedPlaybackEnabled,
        Duration embedTokenTtl,
        String pullZoneHost,
        List<String> allowedMimeTypes,
        Long maxFileSizeBytes
) {

    private static final String DEFAULT_TUS_ENDPOINT = "https://video.bunnycdn.com/tusupload";
    private static final Duration DEFAULT_UPLOAD_EXPIRATION = Duration.ofHours(24);
    private static final Duration DEFAULT_EMBED_TOKEN_TTL = Duration.ofHours(2);
    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024 * 1024;
    private static final List<String> DEFAULT_ALLOWED_MIME_TYPES = List.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

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

    public boolean resolvedSignedPlaybackEnabled() {
        return signedPlaybackEnabled == null || signedPlaybackEnabled;
    }

    public Duration resolvedEmbedTokenTtl() {
        if (embedTokenTtl == null) {
            return DEFAULT_EMBED_TOKEN_TTL;
        }
        return embedTokenTtl;
    }

    public List<String> resolvedAllowedMimeTypes() {
        if (allowedMimeTypes == null || allowedMimeTypes.isEmpty()) {
            return DEFAULT_ALLOWED_MIME_TYPES;
        }
        return allowedMimeTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase())
                .toList();
    }

    public long resolvedMaxFileSizeBytes() {
        if (maxFileSizeBytes == null || maxFileSizeBytes <= 0) {
            return DEFAULT_MAX_FILE_SIZE_BYTES;
        }
        return maxFileSizeBytes;
    }
}
