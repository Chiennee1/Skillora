package com.example.skillora_platform.enrollment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.enrollment.dto.LearningDashboardResponse;
import com.example.skillora_platform.enrollment.service.LearningDashboardFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.LEARNING_API_PREFIX)
@RequiredArgsConstructor
public class LearningDashboardController {

    private final LearningDashboardFacade dashboardFacade;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningDashboardResponse> dashboard(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(dashboardFacade.getDashboard(jwt.getSubject()));
    }
}
