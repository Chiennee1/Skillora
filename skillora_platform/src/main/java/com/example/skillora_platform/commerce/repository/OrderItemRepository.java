package com.example.skillora_platform.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.commerce.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
