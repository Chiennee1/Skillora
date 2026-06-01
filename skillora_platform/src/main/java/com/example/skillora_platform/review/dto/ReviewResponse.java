package com.example.skillora_platform.review.dto;

import java.time.LocalDateTime;

import com.example.skillora_platform.review.entity.ReviewStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewResponse {

    private Long id;
    private Long courseId;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private int rating;
    private String content;
    private ReviewStatus status;
    private long likeCount;
    private boolean likedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
