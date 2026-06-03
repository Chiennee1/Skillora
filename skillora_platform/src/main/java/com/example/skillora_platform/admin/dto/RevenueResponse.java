package com.example.skillora_platform.admin.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RevenueResponse {

    private BigDecimal totalRevenue;
    private long totalPaidOrders;
    private long totalPendingOrders;
    private long totalCancelledOrders;
    private BigDecimal avgOrderValue;
}
