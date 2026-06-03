package com.example.skillora_platform.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.admin.dto.AdminCourseActionRequest;
import com.example.skillora_platform.admin.dto.AdminCourseResponse;
import com.example.skillora_platform.admin.service.AdminCourseService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping(Constants.ADMIN_API_PREFIX + "/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AdminCourseResponse>> listCourses(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return ApiResponse.success(adminCourseService.listCourses(page, size));
    }

    @PatchMapping(Constants.ADMIN_API_PREFIX + "/courses/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCourseResponse> approveCourse(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        return ApiResponse.success("Course approved successfully",
                adminCourseService.approveCourse(id, jwt.getSubject(), httpRequest.getRemoteAddr()));
    }

    @PatchMapping(Constants.ADMIN_API_PREFIX + "/courses/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCourseResponse> rejectCourse(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) AdminCourseActionRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        String reason = request != null ? request.getReason() : null;
        return ApiResponse.success("Course rejected",
                adminCourseService.rejectCourse(id, reason, jwt.getSubject(), httpRequest.getRemoteAddr()));
    }
}
