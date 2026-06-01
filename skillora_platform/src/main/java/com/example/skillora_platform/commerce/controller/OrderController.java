package com.example.skillora_platform.commerce.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.commerce.dto.CheckoutRequest;
import com.example.skillora_platform.commerce.dto.OrderResponse;
import com.example.skillora_platform.commerce.service.OrderService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.ORDER_API_PREFIX)
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully",
                        orderService.checkout(request, jwt.getSubject())));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<OrderResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        return ApiResponse.success(orderService.myOrders(jwt.getSubject(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(
            @PathVariable("id") @Positive Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(orderService.getOrderById(id, jwt.getSubject()));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(
            @PathVariable("id") @Positive Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success("Order cancelled successfully",
                orderService.cancelOrder(id, jwt.getSubject()));
    }
}
