package com.example.skillora_platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.admin.dto.DashboardStatsResponse;
import com.example.skillora_platform.admin.dto.RevenueResponse;
import com.example.skillora_platform.admin.service.AdminDashboardService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping(Constants.ADMIN_API_PREFIX + "/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatsResponse> getDashboard() {
        return ApiResponse.success(adminDashboardService.getDashboardStats());
    }

    @GetMapping(Constants.ADMIN_API_PREFIX + "/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RevenueResponse> getRevenue() {
        return ApiResponse.success(adminDashboardService.getRevenueSummary());
    }
}
