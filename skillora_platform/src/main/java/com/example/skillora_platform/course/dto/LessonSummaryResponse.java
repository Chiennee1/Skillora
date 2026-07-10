package com.example.skillora_platform.course.dto;

import com.example.skillora_platform.course.entity.LessonType;
import com.example.skillora_platform.course.entity.VideoStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonSummaryResponse {

    private Long id;
    private Long sectionId;
    private Long quizId;
    private Long assignmentId;
    private String title;
    private String slug;
    private LessonType type;
    private int durationSeconds;
    private boolean preview;
    private boolean published;
    private int orderIndex;
    private boolean hasVideo;
    private VideoStatus videoStatus;
}
