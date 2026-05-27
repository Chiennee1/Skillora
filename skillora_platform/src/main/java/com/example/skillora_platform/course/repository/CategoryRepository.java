package com.example.skillora_platform.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.skillora_platform.course.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("""
            SELECT c FROM Category c
            LEFT JOIN c.parent p
            WHERE c.active = true
            ORDER BY p.id ASC, c.orderIndex ASC, c.name ASC
            """)
    List<Category> findActiveOrdered();

    Optional<Category> findByIdAndActiveTrue(Integer id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Integer id);
}
