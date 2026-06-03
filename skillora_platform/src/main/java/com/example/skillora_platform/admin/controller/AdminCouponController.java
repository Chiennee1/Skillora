package com.example.skillora_platform.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.admin.dto.AdminCouponCreateRequest;
import com.example.skillora_platform.admin.dto.AdminCouponResponse;
import com.example.skillora_platform.admin.dto.AdminCouponUpdateRequest;
import com.example.skillora_platform.admin.service.AdminCouponService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping(Constants.ADMIN_API_PREFIX + "/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AdminCouponResponse>> listCoupons(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return ApiResponse.success(adminCouponService.listCoupons(page, size));
    }

    @PostMapping(Constants.ADMIN_API_PREFIX + "/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminCouponResponse>> createCoupon(
            @Valid @RequestBody AdminCouponCreateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully",
                        adminCouponService.createCoupon(request, jwt.getSubject(),
                                httpRequest.getRemoteAddr())));
    }

    @PutMapping(Constants.ADMIN_API_PREFIX + "/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCouponResponse> updateCoupon(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminCouponUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        return ApiResponse.success("Coupon updated successfully",
                adminCouponService.updateCoupon(id, request, jwt.getSubject(),
                        httpRequest.getRemoteAddr()));
    }

    @DeleteMapping(Constants.ADMIN_API_PREFIX + "/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateCoupon(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        adminCouponService.deactivateCoupon(id, jwt.getSubject(), httpRequest.getRemoteAddr());
        return ApiResponse.success("Coupon deactivated successfully");
    }
}
