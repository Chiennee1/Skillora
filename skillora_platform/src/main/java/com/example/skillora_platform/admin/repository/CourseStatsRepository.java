package com.example.skillora_platform.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.admin.entity.CourseStats;

public interface CourseStatsRepository extends JpaRepository<CourseStats, Long> {
}
