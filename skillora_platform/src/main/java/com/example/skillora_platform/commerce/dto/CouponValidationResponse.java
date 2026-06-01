package com.example.skillora_platform.commerce.dto;

import java.math.BigDecimal;

import com.example.skillora_platform.commerce.entity.DiscountType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CouponValidationResponse {

    private String code;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
}
