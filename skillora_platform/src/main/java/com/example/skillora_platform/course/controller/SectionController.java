package com.example.skillora_platform.course.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.course.dto.SectionCreateRequest;
import com.example.skillora_platform.course.dto.SectionResponse;
import com.example.skillora_platform.course.dto.SectionUpdateRequest;
import com.example.skillora_platform.course.service.SectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping(Constants.COURSE_API_PREFIX + "/{id}/sections")
    public ApiResponse<List<SectionResponse>> list(
            @PathVariable("id") Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(sectionService.listByCourse(courseId, subject(jwt)));
    }

    @PostMapping(Constants.COURSE_API_PREFIX + "/{id}/sections")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> create(
            @PathVariable("id") Long courseId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SectionCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Section created successfully",
                        sectionService.create(courseId, request, jwt.getSubject())));
    }

    @PutMapping(Constants.SECTION_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<SectionResponse> update(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SectionUpdateRequest request
    ) {
        return ApiResponse.success("Section updated successfully",
                sectionService.update(id, request, jwt.getSubject()));
    }

    @DeleteMapping(Constants.SECTION_API_PREFIX + "/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        sectionService.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    private String subject(Jwt jwt) {
        return jwt == null ? null : jwt.getSubject();
    }
}
