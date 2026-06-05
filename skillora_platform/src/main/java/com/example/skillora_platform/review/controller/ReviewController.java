package com.example.skillora_platform.review.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.review.dto.ReviewCreateRequest;
import com.example.skillora_platform.review.dto.ReviewResponse;
import com.example.skillora_platform.review.dto.ReviewUpdateRequest;
import com.example.skillora_platform.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(Constants.REVIEW_API_PREFIX)
    public ApiResponse<PageResponse<ReviewResponse>> list(
            @RequestParam("courseId") Long courseId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal Jwt jwt) {
        Long currentUserId = jwt != null ? extractUserId(jwt) : null;
        return ApiResponse.success(reviewService.listReviews(courseId, currentUserId, page, size));
    }

    @PostMapping(Constants.REVIEW_API_PREFIX)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully",
                        reviewService.create(request, jwt.getSubject())));
    }

    @PutMapping(Constants.REVIEW_API_PREFIX + "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResponse> update(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ApiResponse.success("Review updated successfully",
                reviewService.update(id, request, jwt.getSubject()));
    }

    @DeleteMapping(Constants.REVIEW_API_PREFIX + "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        reviewService.delete(id, jwt.getSubject());
        return ApiResponse.success("Review deleted successfully");
    }

    /**
     * Like a review. Idempotent.
     */
    @PostMapping(Constants.REVIEW_API_PREFIX + "/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> like(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        reviewService.like(id, jwt.getSubject());
        return ApiResponse.success("Review liked successfully");
    }

    /**
     * Unlike a review. Idempotent.
     */
    @DeleteMapping(Constants.REVIEW_API_PREFIX + "/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> unlike(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        reviewService.unlike(id, jwt.getSubject());
        return ApiResponse.success("Review unliked successfully");
    }

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        return null;
    }
}
