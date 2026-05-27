package com.example.skillora_platform.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.CourseRequirement;

public interface CourseRequirementRepository extends JpaRepository<CourseRequirement, Long> {
}
