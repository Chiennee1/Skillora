package com.example.skillora_platform.course.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.LessonType;
import com.example.skillora_platform.course.entity.VideoStatus;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoursePublishingReadinessService {

    private static final int MAX_TITLES_IN_ERROR = 3;

    private final LessonRepository lessonRepository;

    public void requirePublishedVideoLessonsReady(Long courseId) {
        List<String> blockedLessons = lessonRepository.findPublishedLessonsByCourseId(courseId).stream()
                .filter(lesson -> lesson.getType() == LessonType.VIDEO)
                .filter(lesson -> lesson.getVideo() == null
                        || lesson.getVideo().getStatus() != VideoStatus.READY)
                .map(Lesson::getTitle)
                .toList();

        if (blockedLessons.isEmpty()) {
            return;
        }

        String names = String.join(", ", blockedLessons.stream()
                .limit(MAX_TITLES_IN_ERROR)
                .toList());
        if (blockedLessons.size() > MAX_TITLES_IN_ERROR) {
            names += ", and %d more".formatted(blockedLessons.size() - MAX_TITLES_IN_ERROR);
        }
        throw new BusinessException(
                "All published VIDEO lessons must have READY video before review: " + names,
                HttpStatus.CONFLICT);
    }
}
