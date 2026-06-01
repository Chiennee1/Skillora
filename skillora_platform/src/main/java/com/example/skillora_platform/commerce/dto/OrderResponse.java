package com.example.skillora_platform.commerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.skillora_platform.commerce.entity.OrderStatus;
import com.example.skillora_platform.commerce.entity.PaymentGateway;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private PaymentGateway paymentGateway;
    private String gatewayTransactionId;
    private LocalDateTime paidAt;
    private String failureReason;
    private Long couponId;
    private String couponCode;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
