package com.example.skillora_platform.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseTotalsService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void refreshTotals(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        course.setTotalLessons((int) lessonRepository.countActiveLessonsByCourseId(courseId));
        course.setTotalDurationSeconds((int) lessonRepository.sumActiveDurationSecondsByCourseId(courseId));
        courseRepository.save(course);
    }
}
