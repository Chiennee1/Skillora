package com.example.skillora_platform.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.dto.AdminCouponCreateRequest;
import com.example.skillora_platform.admin.dto.AdminCouponResponse;
import com.example.skillora_platform.admin.dto.AdminCouponUpdateRequest;
import com.example.skillora_platform.commerce.entity.Coupon;
import com.example.skillora_platform.commerce.repository.CouponRepository;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCouponService {

    private final CouponRepository couponRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponse<AdminCouponResponse> listCoupons(int page, int size) {
        Page<AdminCouponResponse> result = couponRepository.findAll(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional
    public AdminCouponResponse createCoupon(AdminCouponCreateRequest request,
                                             String adminEmail, String ipAddress) {
        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new BusinessException("Coupon code already exists: " + request.getCode(), HttpStatus.CONFLICT);
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxUses(request.getMaxUses())
                .usedCount(0)
                .minOrderAmount(request.getMinOrderAmount())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();

        coupon = couponRepository.save(coupon);

        auditLogService.log(adminEmail, "COUPON", coupon.getId(), "CREATE_COUPON",
                null,
                "{\"code\":\"" + coupon.getCode() + "\",\"discountType\":\"" + coupon.getDiscountType()
                        + "\",\"discountValue\":" + coupon.getDiscountValue() + "}",
                ipAddress, null);

        log.info("Admin {} created coupon {} (id={})", adminEmail, coupon.getCode(), coupon.getId());
        return toResponse(coupon);
    }

    @Transactional
    public AdminCouponResponse updateCoupon(Long id, AdminCouponUpdateRequest request,
                                             String adminEmail, String ipAddress) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        if (request.getName() != null) {
            coupon.setName(request.getName());
        }
        if (request.getDiscountType() != null) {
            coupon.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            coupon.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxUses() != null) {
            coupon.setMaxUses(request.getMaxUses());
        }
        if (request.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getStartsAt() != null) {
            coupon.setStartsAt(request.getStartsAt());
        }
        if (request.getExpiresAt() != null) {
            coupon.setExpiresAt(request.getExpiresAt());
        }
        if (request.getActive() != null) {
            coupon.setActive(request.getActive());
        }

        coupon = couponRepository.save(coupon);

        auditLogService.log(adminEmail, "COUPON", id, "UPDATE_COUPON",
                null, null, ipAddress, null);

        log.info("Admin {} updated coupon {} (id={})", adminEmail, coupon.getCode(), id);
        return toResponse(coupon);
    }

    @Transactional
    public void deactivateCoupon(Long id, String adminEmail, String ipAddress) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        coupon.setActive(false);
        couponRepository.save(coupon);

        auditLogService.log(adminEmail, "COUPON", id, "DEACTIVATE_COUPON",
                "{\"active\":true}", "{\"active\":false}", ipAddress, null);

        log.info("Admin {} deactivated coupon {} (id={})", adminEmail, coupon.getCode(), id);
    }

    private AdminCouponResponse toResponse(Coupon coupon) {
        return AdminCouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .maxUses(coupon.getMaxUses())
                .usedCount(coupon.getUsedCount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .startsAt(coupon.getStartsAt())
                .expiresAt(coupon.getExpiresAt())
                .active(coupon.isActive())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
