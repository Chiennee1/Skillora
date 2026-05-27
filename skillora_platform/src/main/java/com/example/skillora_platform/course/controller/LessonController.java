package com.example.skillora_platform.course.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.course.dto.LessonCreateRequest;
import com.example.skillora_platform.course.dto.LessonResourceCreateRequest;
import com.example.skillora_platform.course.dto.LessonResourceResponse;
import com.example.skillora_platform.course.dto.LessonResourceUpdateRequest;
import com.example.skillora_platform.course.dto.LessonResponse;
import com.example.skillora_platform.course.dto.LessonUpdateRequest;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlRequest;
import com.example.skillora_platform.course.dto.LessonVideoUploadUrlResponse;
import com.example.skillora_platform.course.service.LessonService;
import com.example.skillora_platform.course.service.LessonVideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final LessonVideoService lessonVideoService;

    @PostMapping(Constants.SECTION_API_PREFIX + "/{id}/lessons")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> create(
            @PathVariable("id") Long sectionId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LessonCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson created successfully",
                        lessonService.create(sectionId, request, jwt.getSubject())));
    }

    @GetMapping(Constants.LESSON_API_PREFIX + "/{id}")
    public ApiResponse<LessonResponse> get(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(lessonService.get(id, subject(jwt)));
    }

    @PutMapping(Constants.LESSON_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<LessonResponse> update(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LessonUpdateRequest request
    ) {
        return ApiResponse.success("Lesson updated successfully",
                lessonService.update(id, request, jwt.getSubject()));
    }

    @DeleteMapping(Constants.LESSON_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        lessonService.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Constants.LESSON_API_PREFIX + "/{id}/resources")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessonResourceResponse>> addResource(
            @PathVariable("id") Long lessonId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LessonResourceCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson resource created successfully",
                        lessonService.addResource(lessonId, request, jwt.getSubject())));
    }

    @PutMapping(Constants.LESSON_RESOURCE_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<LessonResourceResponse> updateResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LessonResourceUpdateRequest request
    ) {
        return ApiResponse.success("Lesson resource updated successfully",
                lessonService.updateResource(id, request, jwt.getSubject()));
    }

    @DeleteMapping(Constants.LESSON_RESOURCE_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        lessonService.deleteResource(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Constants.LESSON_API_PREFIX + "/{id}/video/upload-url")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<LessonVideoUploadUrlResponse> createUploadUrl(
            @PathVariable("id") Long lessonId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LessonVideoUploadUrlRequest request
    ) {
        return ApiResponse.success("Lesson video upload URL created successfully",
                lessonVideoService.createUploadUrl(lessonId, request, jwt.getSubject()));
    }

    private String subject(Jwt jwt) {
        return jwt == null ? null : jwt.getSubject();
    }
}
