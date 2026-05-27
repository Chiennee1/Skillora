package com.example.skillora_platform.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {

    Optional<Section> findByIdAndDeletedAtIsNull(Long id);

    List<Section> findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(Long courseId);

    boolean existsByCourseIdAndOrderIndex(Long courseId, int orderIndex);

    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, int orderIndex, Long id);
}
