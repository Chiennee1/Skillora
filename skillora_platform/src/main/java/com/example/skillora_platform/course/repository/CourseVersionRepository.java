package com.example.skillora_platform.course.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.CourseVersion;
import com.example.skillora_platform.course.entity.CourseVersionStatus;

public interface CourseVersionRepository extends JpaRepository<CourseVersion, Long> {

    Page<CourseVersion> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseVersion> findByStatus(CourseVersionStatus status, Pageable pageable);

    Optional<CourseVersion> findByCourseIdAndId(Long courseId, Long id);

    Optional<CourseVersion> findFirstByCourseIdOrderByVersionNumberDesc(Long courseId);

    boolean existsByCourseIdAndStatusIn(Long courseId, Collection<CourseVersionStatus> statuses);
}
