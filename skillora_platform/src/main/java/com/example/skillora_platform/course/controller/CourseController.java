package com.example.skillora_platform.course.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.dto.CourseCreateRequest;
import com.example.skillora_platform.course.dto.CourseResponse;
import com.example.skillora_platform.course.dto.CourseSummaryResponse;
import com.example.skillora_platform.course.dto.CourseUpdateRequest;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.service.CourseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.COURSE_API_PREFIX)
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<PageResponse<CourseSummaryResponse>> list(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "level", required = false) CourseLevel level,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return ApiResponse.success(courseService.listPublic(search, level, categoryId, page, size, sort));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<PageResponse<CourseSummaryResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return ApiResponse.success(courseService.listMine(jwt.getSubject(), page, size, sort));
    }

    @GetMapping("/{idOrSlug}")
    public ApiResponse<CourseResponse> get(
            @PathVariable("idOrSlug") String idOrSlug,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(courseService.getByIdOrSlug(idOrSlug, subject(jwt)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CourseCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully",
                        courseService.create(request, jwt.getSubject())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseResponse> update(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CourseUpdateRequest request
    ) {
        return ApiResponse.success("Course updated successfully",
                courseService.update(id, request, jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        courseService.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseResponse> publish(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Course submitted for review", courseService.publish(id, jwt.getSubject()));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<CourseResponse> archive(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Course archived successfully", courseService.archive(id, jwt.getSubject()));
    }

    private String subject(Jwt jwt) {
        return jwt == null ? null : jwt.getSubject();
    }
}
