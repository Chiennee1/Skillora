package com.example.skillora_platform.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.LessonResource;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {

    Optional<LessonResource> findByIdAndLessonDeletedAtIsNull(Long id);

    List<LessonResource> findByLessonIdOrderByOrderIndexAscIdAsc(Long lessonId);
}
