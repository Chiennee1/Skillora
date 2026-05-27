package com.example.skillora_platform.user.controller;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.user.dto.ProfileResponse;
import com.example.skillora_platform.user.dto.ProfileUpdateRequest;
import com.example.skillora_platform.user.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.PROFILE_API_PREFIX)
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(userProfileService.getMyProfile(jwt.getSubject()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ApiResponse.success("Profile updated successfully",
                userProfileService.updateMyProfile(jwt.getSubject(), request));
    }
}
