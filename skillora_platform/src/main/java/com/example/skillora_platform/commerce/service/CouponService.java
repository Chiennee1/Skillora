package com.example.skillora_platform.commerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.commerce.dto.CouponValidateRequest;
import com.example.skillora_platform.commerce.dto.CouponValidationResponse;
import com.example.skillora_platform.commerce.entity.Cart;
import com.example.skillora_platform.commerce.entity.Coupon;
import com.example.skillora_platform.commerce.entity.DiscountType;
import com.example.skillora_platform.commerce.repository.CouponRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final int MONEY_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CouponRepository couponRepository;
    private final CoursePermissionService permissionService;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public CouponValidationResponse validate(CouponValidateRequest request, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Cart cart = cartService.findCartWithItems(actor);
        if (cart == null || cartService.sortedItems(cart).isEmpty()) {
            throw new BusinessException("Cart is empty", HttpStatus.BAD_REQUEST);
        }
        cartService.requireSingleCurrency(cart);

        BigDecimal subtotal = cartService.subtotal(cart);
        String currency = cartService.currencyOrDefault(cart);
        AppliedCoupon appliedCoupon = applyCoupon(request.getCode(), subtotal, LocalDateTime.now());
        Coupon coupon = appliedCoupon.coupon();

        return CouponValidationResponse.builder()
                .code(coupon.getCode())
                .name(coupon.getName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .subtotalAmount(subtotal)
                .discountAmount(appliedCoupon.discountAmount())
                .totalAmount(appliedCoupon.totalAmount())
                .currency(currency)
                .build();
    }

    @Transactional(readOnly = true)
    public AppliedCoupon applyCoupon(String rawCode, BigDecimal subtotal, LocalDateTime now) {
        if (rawCode == null || rawCode.isBlank()) {
            return new AppliedCoupon(null, BigDecimal.ZERO, scale(subtotal));
        }
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Coupon cannot be applied to a zero amount cart", HttpStatus.BAD_REQUEST);
        }

        String code = rawCode.trim().toUpperCase();
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        validateCoupon(coupon, subtotal, now);

        BigDecimal discountAmount = calculateDiscount(coupon, subtotal);
        BigDecimal totalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        return new AppliedCoupon(coupon, scale(discountAmount), scale(totalAmount));
    }

    private void validateCoupon(Coupon coupon, BigDecimal subtotal, LocalDateTime now) {
        if (!coupon.isActive()) {
            throw new BusinessException("Coupon is inactive", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getStartsAt() != null && coupon.getStartsAt().isAfter(now)) {
            throw new BusinessException("Coupon is not active yet", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(now)) {
            throw new BusinessException("Coupon has expired", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new BusinessException("Coupon usage limit reached", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException("Order amount does not meet coupon minimum", HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal.multiply(coupon.getDiscountValue())
                    .divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }
        return scale(discount.min(subtotal).max(BigDecimal.ZERO));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
