package com.example.skillora_platform.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.skillora_platform.commerce.entity.DiscountType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCouponCreateRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Coupon code must be at most 50 characters")
    private String code;

    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private Integer maxUses;

    @DecimalMin(value = "0.00", message = "Min order amount must be non-negative")
    private BigDecimal minOrderAmount;

    private LocalDateTime startsAt;

    private LocalDateTime expiresAt;
}
