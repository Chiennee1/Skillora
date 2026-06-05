package com.example.skillora_platform.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCreateRequest {

    @NotNull(message = "Order id is required")
    @Positive(message = "Order id must be positive")
    private Long orderId;
}
