package com.example.skillora_platform.course.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import com.example.skillora_platform.config.BunnyStreamProperties;
import com.example.skillora_platform.course.entity.LessonVideo;
import com.example.skillora_platform.course.entity.VideoProvider;
import com.example.skillora_platform.course.entity.VideoStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BunnyStreamUrlService {

    private static final String PLAYER_BASE_URL = "https://player.mediadelivery.net";

    private final BunnyStreamProperties properties;

    public String storedPlaybackUrl(String videoId) {
        if (!hasLibraryId() || isBlank(videoId)) {
            return null;
        }
        return "%s/play/%s/%s".formatted(PLAYER_BASE_URL, properties.libraryId().trim(), videoId.trim());
    }

    public String storedEmbedUrl(String videoId) {
        if (!hasLibraryId() || isBlank(videoId)) {
            return null;
        }
        return "%s/embed/%s/%s".formatted(PLAYER_BASE_URL, properties.libraryId().trim(), videoId.trim());
    }

    public String storedHlsUrl(String videoId) {
        if (isBlank(videoId) || isBlank(properties.pullZoneHost())) {
            return null;
        }
        return "%s/%s/playlist.m3u8".formatted(cdnBaseUrl(), videoId.trim());
    }

    public String storedThumbnailUrl(String videoId) {
        if (isBlank(videoId) || isBlank(properties.pullZoneHost())) {
            return null;
        }
        return "%s/%s/thumbnail.jpg".formatted(cdnBaseUrl(), videoId.trim());
    }

    public String responseEmbedUrl(LessonVideo video) {
        if (!isReadyBunnyVideo(video)) {
            return null;
        }
        String videoId = video.getAssetId();
        String baseUrl = storedEmbedUrl(videoId);
        if (baseUrl == null) {
            return null;
        }
        if (!properties.resolvedSignedPlaybackEnabled()) {
            return withPlayerDefaults(baseUrl);
        }
        if (isBlank(properties.tokenSecurityKey())) {
            return null;
        }
        long expires = Instant.now().plus(properties.resolvedEmbedTokenTtl()).getEpochSecond();
        String token = sha256Hex(properties.tokenSecurityKey().trim() + videoId.trim() + expires);
        return "%s?token=%s&expires=%d&preload=true&responsive=true".formatted(baseUrl, token, expires);
    }

    public String responsePlaybackUrl(LessonVideo video) {
        if (video == null || properties.resolvedSignedPlaybackEnabled()) {
            return null;
        }
        return video.getPlaybackUrl();
    }

    public String responseHlsUrl(LessonVideo video) {
        if (video == null || properties.resolvedSignedPlaybackEnabled()) {
            return null;
        }
        return video.getHlsUrl();
    }

    private boolean isReadyBunnyVideo(LessonVideo video) {
        return video != null
                && video.getProvider() == VideoProvider.BUNNY
                && video.getStatus() == VideoStatus.READY
                && !isBlank(video.getAssetId());
    }

    private String withPlayerDefaults(String baseUrl) {
        return baseUrl + "?preload=true&responsive=true";
    }

    private boolean hasLibraryId() {
        return !isBlank(properties.libraryId());
    }

    private String cdnBaseUrl() {
        String host = properties.pullZoneHost().trim();
        String base = host.startsWith("http://") || host.startsWith("https://")
                ? host
                : "https://" + host;
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
