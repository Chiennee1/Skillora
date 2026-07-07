package com.example.skillora_platform.course.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.dto.CourseVersionResponse;
import com.example.skillora_platform.course.dto.CourseVersionUpdateRequest;
import com.example.skillora_platform.course.service.CourseVersionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseVersionController {

    private final CourseVersionService courseVersionService;

    @PostMapping(Constants.COURSE_API_PREFIX + "/{courseId}/versions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseVersionResponse>> createDraft(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course version draft created",
                        courseVersionService.createDraftVersion(courseId, jwt.getSubject())));
    }

    @GetMapping(Constants.COURSE_API_PREFIX + "/{courseId}/versions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<PageResponse<CourseVersionResponse>> listVersions(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        return ApiResponse.success(courseVersionService.listCourseVersions(courseId, jwt.getSubject(), page, size));
    }

    @GetMapping(Constants.COURSE_API_PREFIX + "/{courseId}/versions/{versionId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseVersionResponse> getVersion(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId") Long versionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(courseVersionService.getVersion(courseId, versionId, jwt.getSubject()));
    }

    @PutMapping(Constants.COURSE_API_PREFIX + "/{courseId}/versions/{versionId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseVersionResponse> updateDraft(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId") Long versionId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CourseVersionUpdateRequest request
    ) {
        return ApiResponse.success("Course version draft updated",
                courseVersionService.updateDraftVersion(courseId, versionId, request, jwt.getSubject()));
    }

    @PatchMapping(Constants.COURSE_API_PREFIX + "/{courseId}/versions/{versionId}/submit")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseVersionResponse> submit(
            @PathVariable("courseId") Long courseId,
            @PathVariable("versionId") Long versionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success("Course version submitted for review",
                courseVersionService.submitVersionForReview(courseId, versionId, jwt.getSubject()));
    }
}
