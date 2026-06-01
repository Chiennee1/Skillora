package com.example.skillora_platform.commerce.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResponse {

    private CommerceCourseResponse course;
    private LocalDateTime addedAt;
}
