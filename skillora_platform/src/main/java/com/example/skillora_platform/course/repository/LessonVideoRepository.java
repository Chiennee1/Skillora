package com.example.skillora_platform.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.course.entity.LessonVideo;
import com.example.skillora_platform.course.entity.VideoProvider;

public interface LessonVideoRepository extends JpaRepository<LessonVideo, Long> {

    Optional<LessonVideo> findByLessonId(Long lessonId);

    Optional<LessonVideo> findByProviderAndAssetId(VideoProvider provider, String assetId);
}
