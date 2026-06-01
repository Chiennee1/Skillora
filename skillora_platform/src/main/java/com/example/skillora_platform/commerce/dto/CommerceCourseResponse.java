package com.example.skillora_platform.commerce.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommerceCourseResponse {

    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private Long instructorId;
    private String instructorName;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private String currency;
}
