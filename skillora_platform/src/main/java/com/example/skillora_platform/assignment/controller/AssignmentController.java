package com.example.skillora_platform.assignment.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.assignment.dto.AssignmentCreateRequest;
import com.example.skillora_platform.assignment.dto.AssignmentGradeRequest;
import com.example.skillora_platform.assignment.dto.AssignmentResponse;
import com.example.skillora_platform.assignment.dto.AssignmentSubmissionResponse;
import com.example.skillora_platform.assignment.dto.AssignmentSubmitRequest;
import com.example.skillora_platform.assignment.entity.SubmissionStatus;
import com.example.skillora_platform.assignment.service.AssignmentGradingService;
import com.example.skillora_platform.assignment.service.AssignmentService;
import com.example.skillora_platform.assignment.service.AssignmentSubmissionService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final AssignmentGradingService assignmentGradingService;

    @PostMapping(Constants.ASSIGNMENT_API_PREFIX)
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AssignmentCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully",
                        assignmentService.create(request, jwt.getSubject())));
    }

    @GetMapping(Constants.ASSIGNMENT_API_PREFIX + "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AssignmentResponse> get(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(assignmentService.get(id, jwt.getSubject()));
    }

    @PostMapping(Constants.ASSIGNMENT_API_PREFIX + "/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> submit(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AssignmentSubmitRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment submitted successfully",
                        assignmentSubmissionService.submit(id, request, jwt.getSubject())));
    }

    @GetMapping(Constants.ASSIGNMENT_API_PREFIX + "/{id}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<PageResponse<AssignmentSubmissionResponse>> submissions(
            @PathVariable("id") Long id,
            @RequestParam(name = "status", required = false) SubmissionStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                assignmentSubmissionService.listSubmissions(id, status, page, size, jwt.getSubject()));
    }

    @PatchMapping(Constants.SUBMISSION_API_PREFIX + "/{id}/grade")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<AssignmentSubmissionResponse> grade(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AssignmentGradeRequest request
    ) {
        return ApiResponse.success("Assignment submission updated successfully",
                assignmentGradingService.grade(id, request, jwt.getSubject()));
    }
}
