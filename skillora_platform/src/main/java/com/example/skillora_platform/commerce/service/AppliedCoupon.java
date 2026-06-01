package com.example.skillora_platform.commerce.service;

import java.math.BigDecimal;

import com.example.skillora_platform.commerce.entity.Coupon;

record AppliedCoupon(Coupon coupon, BigDecimal discountAmount, BigDecimal totalAmount) {
}
