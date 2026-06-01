package com.example.skillora_platform.commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.commerce.entity.PaymentGateway;
import com.example.skillora_platform.commerce.entity.PaymentTransaction;
import com.example.skillora_platform.commerce.entity.TxStatus;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    Optional<PaymentTransaction> findByGatewayAndGatewayTransactionId(
            PaymentGateway gateway, String gatewayTransactionId);

    Optional<PaymentTransaction> findByGatewayAndGatewayOrderId(
            PaymentGateway gateway, String gatewayOrderId);

    boolean existsByOrderIdAndStatus(Long orderId, TxStatus status);
}
