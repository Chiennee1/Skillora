package com.example.skillora_platform.commerce.controller;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.commerce.dto.CouponValidateRequest;
import com.example.skillora_platform.commerce.dto.CouponValidationResponse;
import com.example.skillora_platform.commerce.service.CouponService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.COUPON_API_PREFIX)
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ApiResponse<CouponValidationResponse> validate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CouponValidateRequest request
    ) {
        return ApiResponse.success(couponService.validate(request, jwt.getSubject()));
    }
}
