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

import com.example.skillora_platform.admin.dto.AdminUserResponse;
import com.example.skillora_platform.admin.dto.AdminUserStatusRequest;
import com.example.skillora_platform.admin.service.AdminUserService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.user.entity.UserStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping(Constants.ADMIN_API_PREFIX + "/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AdminUserResponse>> listUsers(
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return ApiResponse.success(adminUserService.listUsers(status, role, search, page, size));
    }

    @GetMapping(Constants.ADMIN_API_PREFIX + "/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable("id") Long id) {
        return ApiResponse.success(adminUserService.getUser(id));
    }

    @PatchMapping(Constants.ADMIN_API_PREFIX + "/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminUserResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminUserStatusRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        return ApiResponse.success("User status updated successfully",
                adminUserService.updateStatus(id, request, jwt.getSubject(), httpRequest.getRemoteAddr()));
    }
}
