package com.example.skillora_platform.commerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemResponse {

    private Long id;
    private Long courseId;
    private String courseTitleSnapshot;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private LocalDateTime createdAt;
}
