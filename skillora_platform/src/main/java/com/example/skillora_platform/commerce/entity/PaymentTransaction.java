package com.example.skillora_platform.commerce.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.skillora_platform.common.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "request_id", length = 150)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "gateway", nullable = false, length = 30)
    private PaymentGateway gateway;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 30)
    private TxStatus status;

    @Column(name = "result_code", length = 50)
    private String resultCode;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "gateway_order_id", length = 150)
    private String gatewayOrderId;

    @Column(name = "gateway_transaction_id", length = 150)
    private String gatewayTransactionId;

    @Column(name = "pay_type", length = 50)
    private String payType;

    @Column(name = "pay_url", columnDefinition = "TEXT")
    private String payUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_request", columnDefinition = "JSON")
    private String rawRequest;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "JSON")
    private String rawResponse;

    @Column(name = "response_time")
    private Long responseTime;
}
