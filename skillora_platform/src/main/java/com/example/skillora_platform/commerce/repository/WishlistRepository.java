package com.example.skillora_platform.commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.skillora_platform.commerce.entity.Wishlist;
import com.example.skillora_platform.commerce.entity.WishlistId;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    void deleteByUserIdAndCourseId(Long userId, Long courseId);

    @Query("""
            SELECT w FROM Wishlist w
            JOIN FETCH w.course c
            JOIN FETCH c.instructor
            WHERE w.userId = :userId
            ORDER BY w.createdAt DESC
            """)
    List<Wishlist> findByUserIdWithCourses(@Param("userId") Long userId);
}
