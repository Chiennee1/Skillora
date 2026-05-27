package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;

import com.example.skillora_platform.course.entity.VideoProvider;
import com.example.skillora_platform.course.entity.VideoStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonVideoResponse {

    private Long id;
    private VideoProvider provider;
    private String assetId;
    private String playbackUrl;
    private String hlsUrl;
    private String thumbnailUrl;
    private int durationSeconds;
    private Long sizeBytes;
    private String mimeType;
    private VideoStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
