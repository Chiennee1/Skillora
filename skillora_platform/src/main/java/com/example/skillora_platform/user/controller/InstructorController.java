package com.example.skillora_platform.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.user.dto.InstructorProfileResponse;
import com.example.skillora_platform.user.service.InstructorProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.INSTRUCTOR_API_PREFIX)
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorProfileService instructorProfileService;

    @GetMapping("/{id}")
    public ApiResponse<InstructorProfileResponse> getInstructor(@PathVariable("id") Long id) {
        return ApiResponse.success(instructorProfileService.getPublicInstructorProfile(id));
    }
}
