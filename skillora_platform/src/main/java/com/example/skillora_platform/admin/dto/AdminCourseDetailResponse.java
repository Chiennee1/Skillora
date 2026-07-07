package com.example.skillora_platform.admin.dto;

import java.util.List;

import com.example.skillora_platform.course.dto.CourseResponse;
import com.example.skillora_platform.course.dto.SectionResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminCourseDetailResponse {

    private CourseResponse course;
    private List<SectionResponse> sections;
}
