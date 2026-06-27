package com.example.skillora_platform.commerce.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.skillora_platform.commerce.dto.MomoIpnRequest;
import com.example.skillora_platform.commerce.dto.PaymentCreateRequest;
import com.example.skillora_platform.commerce.dto.PaymentCreateResponse;
import com.example.skillora_platform.commerce.dto.VnPayIpnResponse;
import com.example.skillora_platform.commerce.service.PaymentService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.config.PaymentProperties;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.PAYMENT_API_PREFIX)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentProperties paymentProperties;

    @PostMapping("/vnpay/create")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createVnPayPayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("VNPay payment URL created successfully",
                        paymentService.createVnPayPayment(
                                request.getOrderId(), jwt.getSubject(), clientIp(httpRequest))));
    }

    @PostMapping("/momo/create")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createMomoPayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("MoMo payment URL created successfully",
                        paymentService.createMomoPayment(request.getOrderId(), jwt.getSubject())));
    }

    @GetMapping("/vnpay/return")
    public RedirectView vnpayReturn(@RequestParam Map<String, String> params) {
        return paymentService.handleVnPayReturn(params);
    }

    @PostMapping("/vnpay/ipn")
    public VnPayIpnResponse vnpayIpn(@RequestParam Map<String, String> params) {
        return paymentService.handleVnPayIpn(params);
    }

    @GetMapping("/vnpay/ipn")
    public VnPayIpnResponse vnpayIpnGet(@RequestParam Map<String, String> params) {
        return paymentService.handleVnPayIpn(params);
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody MomoIpnRequest request) {
        paymentService.handleMomoIpn(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/momo/return")
    public RedirectView momoReturn(@RequestParam Map<String, String> params) {
        String redirectUrl = UriComponentsBuilder.fromUriString(paymentProperties.resolvedResultUrl())
                .queryParam("gateway", "MOMO")
                .queryParam("orderId", params.get("orderId"))
                .queryParam("status", "0".equals(params.get("resultCode")) ? "PAID" : "FAILED")
                .queryParam("code", params.get("resultCode"))
                .build()
                .toUriString();
        return new RedirectView(redirectUrl);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
