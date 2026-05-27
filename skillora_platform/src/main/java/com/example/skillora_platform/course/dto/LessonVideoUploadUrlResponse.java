package com.example.skillora_platform.course.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonVideoUploadUrlResponse {

    private Long lessonVideoId;
    private String videoId;
    private String uploadUrl;
    private Map<String, String> headers;
    private Map<String, String> metadata;
    private Instant expiresAt;
}
