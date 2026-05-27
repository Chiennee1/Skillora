package com.example.skillora_platform.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.CourseOutcome;

public interface CourseOutcomeRepository extends JpaRepository<CourseOutcome, Long> {
}
