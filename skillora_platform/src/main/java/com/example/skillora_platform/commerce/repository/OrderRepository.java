package com.example.skillora_platform.commerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.commerce.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"coupon", "items", "items.course"})
    Page<Order> findDistinctByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
