package com.example.skillora_platform.course.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.config.BunnyStreamProperties;
import com.example.skillora_platform.course.dto.BunnyVideoCreated;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlRequest;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlResponse;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.LessonType;
import com.example.skillora_platform.course.entity.LessonVideo;
import com.example.skillora_platform.course.entity.VideoProvider;
import com.example.skillora_platform.course.entity.VideoStatus;
import com.example.skillora_platform.course.repository.LessonVideoRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonVideoService {

    private static final Duration MIN_UPLOAD_EXPIRATION = Duration.ofHours(1);

    private final LessonService lessonService;
    private final CoursePermissionService permissionService;
    private final LessonVideoRepository lessonVideoRepository;
    private final BunnyStreamClient bunnyStreamClient;
    private final BunnyStreamProperties bunnyStreamProperties;
    private final BunnyStreamUrlService bunnyStreamUrlService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LessonVideoUploadUrlResponse createUploadUrl(
            Long lessonId,
            LessonVideoUploadUrlRequest request,
            String actorEmail
    ) {
        Lesson lesson = lessonService.findActiveLesson(lessonId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(lesson.getSection().getCourse(), actor);
        if (lesson.getSection().getCourse().getStatus() == CourseStatus.PUBLISHED
                && !permissionService.isAdmin(actor)) {
            throw new BusinessException(
                    "Published courses must be changed through a course version draft",
                    HttpStatus.CONFLICT);
        }
        validateUploadRequest(lesson, request);
        requireSignatureConfig();

        BunnyVideoCreated createdVideo = bunnyStreamClient.createVideo(lesson.getTitle());
        LessonVideo lessonVideo = lessonVideoRepository.findByLessonId(lesson.getId())
                .orElseGet(() -> LessonVideo.builder()
                        .lesson(lesson)
                        .provider(VideoProvider.BUNNY)
                        .build());
        lessonVideo.setProvider(VideoProvider.BUNNY);
        lessonVideo.setAssetId(createdVideo.videoId());
        lessonVideo.setDurationSeconds(lesson.getDurationSeconds());
        lessonVideo.setSizeBytes(request.getFileSizeBytes());
        lessonVideo.setMimeType(request.getMimeType().trim());
        lessonVideo.setStatus(VideoStatus.UPLOADING);
        lessonVideo.setErrorMessage(null);
        lessonVideo.setOriginalFileUrl(null);
        lessonVideo.setPlaybackUrl(null);
        lessonVideo.setHlsUrl(null);
        lessonVideo.setThumbnailUrl(null);
        LessonVideo savedVideo = lessonVideoRepository.save(lessonVideo);

        Instant expiresAt = Instant.now().plus(uploadExpiration());
        long expiration = expiresAt.getEpochSecond();
        String signature = authorizationSignature(createdVideo.videoId(), expiration);

        return LessonVideoUploadUrlResponse.builder()
                .lessonVideoId(savedVideo.getId())
                .videoId(createdVideo.videoId())
                .uploadUrl(bunnyStreamProperties.resolvedTusEndpoint())
                .headers(Map.of(
                        "AuthorizationSignature", signature,
                        "AuthorizationExpire", String.valueOf(expiration),
                        "VideoId", createdVideo.videoId(),
                        "LibraryId", bunnyStreamProperties.libraryId()
                ))
                .metadata(Map.of(
                        "fileName", request.getFileName().trim(),
                        "filetype", request.getMimeType().trim(),
                        "title", lesson.getTitle()
                ))
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional
    public void handleBunnyWebhook(
            String rawBody,
            String signatureVersion,
            String signatureAlgorithm,
            String signature
    ) {
        requireWebhookConfig();
        if (!validWebhookSignature(rawBody, signatureVersion, signatureAlgorithm, signature)) {
            throw new BusinessException("Invalid Bunny Stream webhook signature", HttpStatus.UNAUTHORIZED);
        }

        JsonNode payload = parseWebhookBody(rawBody);
        String libraryId = payload.path("VideoLibraryId").asText();
        String videoGuid = payload.path("VideoGuid").asText();
        int statusCode = payload.path("Status").asInt(-1);
        if (libraryId.isBlank() || videoGuid.isBlank() || statusCode < 0) {
            throw new BusinessException("Invalid Bunny Stream webhook payload", HttpStatus.BAD_REQUEST);
        }
        if (!libraryId.equals(bunnyStreamProperties.libraryId())) {
            throw new BusinessException("Invalid Bunny Stream library id", HttpStatus.BAD_REQUEST);
        }

        VideoStatus nextStatus = mapWebhookStatus(statusCode);
        if (nextStatus == null) {
            return;
        }

        lessonVideoRepository.findByProviderAndAssetId(VideoProvider.BUNNY, videoGuid)
                .ifPresent(video -> applyWebhookStatus(video, nextStatus, statusCode));
    }

    private void validateUploadRequest(Lesson lesson, LessonVideoUploadUrlRequest request) {
        if (lesson.getType() != LessonType.VIDEO) {
            throw new BusinessException("Only VIDEO lessons can receive video uploads", HttpStatus.BAD_REQUEST);
        }
        if (request.getFileSizeBytes() == null || request.getFileSizeBytes() <= 0) {
            throw new BusinessException("File size must be positive", HttpStatus.BAD_REQUEST);
        }
        if (request.getFileSizeBytes() > bunnyStreamProperties.resolvedMaxFileSizeBytes()) {
            throw new BusinessException("Video file size exceeds the configured limit", HttpStatus.BAD_REQUEST);
        }
        String mimeType = request.getMimeType().trim().toLowerCase();
        if (!bunnyStreamProperties.resolvedAllowedMimeTypes().contains(mimeType)) {
            throw new BusinessException("Only video MIME types are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireSignatureConfig() {
        if (bunnyStreamProperties.libraryId() == null || bunnyStreamProperties.libraryId().isBlank()
                || bunnyStreamProperties.apiKey() == null || bunnyStreamProperties.apiKey().isBlank()) {
            throw new BusinessException("Bunny Stream is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void requireWebhookConfig() {
        if (bunnyStreamProperties.libraryId() == null || bunnyStreamProperties.libraryId().isBlank()
                || bunnyStreamProperties.webhookSigningSecret() == null
                || bunnyStreamProperties.webhookSigningSecret().isBlank()) {
            throw new BusinessException("Bunny Stream webhook is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Duration uploadExpiration() {
        Duration configuredExpiration = bunnyStreamProperties.resolvedUploadExpiration();
        if (configuredExpiration.compareTo(MIN_UPLOAD_EXPIRATION) < 0) {
            throw new BusinessException("Bunny upload expiration must be at least 1 hour",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return configuredExpiration;
    }

    private String authorizationSignature(String videoId, long expiration) {
        String value = bunnyStreamProperties.libraryId() + bunnyStreamProperties.apiKey() + expiration + videoId;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }

    private boolean validWebhookSignature(
            String rawBody,
            String signatureVersion,
            String signatureAlgorithm,
            String signature
    ) {
        if (!"v1".equals(signatureVersion) || !"hmac-sha256".equals(signatureAlgorithm)
                || signature == null || signature.isBlank()) {
            return false;
        }
        String expected = hmacSha256(rawBody, bunnyStreamProperties.webhookSigningSecret().trim());
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String hmacSha256(String rawBody, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("HMAC-SHA256 is not available", ex);
        }
    }

    private JsonNode parseWebhookBody(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception ex) {
            throw new BusinessException("Invalid Bunny Stream webhook payload", HttpStatus.BAD_REQUEST);
        }
    }

    private VideoStatus mapWebhookStatus(int statusCode) {
        return switch (statusCode) {
            case 6 -> VideoStatus.UPLOADING;
            case 0, 1, 2, 7 -> VideoStatus.PROCESSING;
            case 3, 4 -> VideoStatus.READY;
            case 5, 8 -> VideoStatus.FAILED;
            default -> null;
        };
    }

    private void applyWebhookStatus(LessonVideo video, VideoStatus nextStatus, int statusCode) {
        if (video.getStatus() == VideoStatus.READY
                && (nextStatus == VideoStatus.UPLOADING || nextStatus == VideoStatus.PROCESSING)) {
            return;
        }
        video.setStatus(nextStatus);
        if (nextStatus == VideoStatus.READY) {
            video.setPlaybackUrl(bunnyStreamUrlService.storedPlaybackUrl(video.getAssetId()));
            video.setHlsUrl(bunnyStreamUrlService.storedHlsUrl(video.getAssetId()));
            video.setThumbnailUrl(bunnyStreamUrlService.storedThumbnailUrl(video.getAssetId()));
            video.setErrorMessage(null);
            return;
        }
        if (nextStatus == VideoStatus.FAILED) {
            video.setErrorMessage("Bunny Stream video processing failed with status " + statusCode);
        }
    }
}
