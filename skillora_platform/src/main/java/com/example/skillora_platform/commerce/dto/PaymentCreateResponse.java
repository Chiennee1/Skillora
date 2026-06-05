package com.example.skillora_platform.commerce.dto;

import java.math.BigDecimal;

import com.example.skillora_platform.commerce.entity.PaymentGateway;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentCreateResponse {

    private Long orderId;
    private Long paymentTransactionId;
    private PaymentGateway gateway;
    private BigDecimal amount;
    private String currency;
    private String payUrl;
}
