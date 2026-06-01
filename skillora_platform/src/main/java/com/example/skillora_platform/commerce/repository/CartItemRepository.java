package com.example.skillora_platform.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.commerce.entity.CartItem;
import com.example.skillora_platform.commerce.entity.CartItemId;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    boolean existsByCartIdAndCourseId(Long cartId, Long courseId);

    void deleteByCartIdAndCourseId(Long cartId, Long courseId);

    @Modifying
    @Query("DELETE FROM CartItem i WHERE i.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
