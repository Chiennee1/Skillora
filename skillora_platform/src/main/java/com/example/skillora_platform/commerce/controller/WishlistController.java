package com.example.skillora_platform.commerce.controller;

import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.commerce.dto.WishlistResponse;
import com.example.skillora_platform.commerce.service.WishlistService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.WISHLIST_API_PREFIX)
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<WishlistResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(wishlistService.getWishlist(jwt.getSubject()));
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(
            @PathVariable("courseId") @Positive Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course added to wishlist",
                        wishlistService.add(courseId, jwt.getSubject())));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> remove(
            @PathVariable("courseId") @Positive Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        wishlistService.remove(courseId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
