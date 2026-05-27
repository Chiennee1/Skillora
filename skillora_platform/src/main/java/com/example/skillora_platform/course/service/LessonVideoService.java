package com.example.skillora_platform.course.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.config.BunnyStreamProperties;
import com.example.skillora_platform.course.dto.BunnyVideoCreated;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlRequest;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlResponse;
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

    @Transactional
    public LessonVideoUploadUrlResponse createUploadUrl(
            Long lessonId,
            LessonVideoUploadUrlRequest request,
            String actorEmail
    ) {
        Lesson lesson = lessonService.findActiveLesson(lessonId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(lesson.getSection().getCourse(), actor);
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

    private void validateUploadRequest(Lesson lesson, LessonVideoUploadUrlRequest request) {
        if (lesson.getType() != LessonType.VIDEO) {
            throw new BusinessException("Only VIDEO lessons can receive video uploads", HttpStatus.BAD_REQUEST);
        }
        if (!request.getMimeType().trim().toLowerCase().startsWith("video/")) {
            throw new BusinessException("Only video MIME types are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireSignatureConfig() {
        if (bunnyStreamProperties.libraryId() == null || bunnyStreamProperties.libraryId().isBlank()
                || bunnyStreamProperties.apiKey() == null || bunnyStreamProperties.apiKey().isBlank()) {
            throw new BusinessException("Bunny Stream is not configured", HttpStatus.SERVICE_UNAVAILABLE);
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
}
