package com.example.skillora_platform.commerce.dto;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @Size(max = 50, message = "Coupon code must not exceed 50 characters")
    private String couponCode;
}
