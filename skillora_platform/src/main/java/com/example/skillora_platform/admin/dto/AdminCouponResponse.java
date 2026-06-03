package com.example.skillora_platform.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminCouponResponse {

    private Long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private Integer maxUses;
    private int usedCount;
    private BigDecimal minOrderAmount;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private LocalDateTime createdAt;
}
