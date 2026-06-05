package com.example.skillora_platform.course.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.user.entity.User;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    Optional<Course> findByIdAndDeletedAtIsNull(Long id);

    Optional<Course> findBySlugAndDeletedAtIsNull(String slug);

    Page<Course> findByInstructorAndDeletedAtIsNull(User instructor, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByStatusAndDeletedAtIsNull(CourseStatus status);

    long countByDeletedAtIsNull();

    Page<Course> findByDeletedAtIsNull(Pageable pageable);
}
