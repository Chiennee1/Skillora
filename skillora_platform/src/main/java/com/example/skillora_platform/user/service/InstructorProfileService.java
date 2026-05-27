package com.example.skillora_platform.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.dto.InstructorProfileResponse;
import com.example.skillora_platform.user.entity.InstructorProfile;
import com.example.skillora_platform.user.entity.UserProfile;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructorProfileService {

    private final InstructorProfileRepository instructorProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public InstructorProfileResponse getPublicInstructorProfile(Long userId) {
        InstructorProfile instructorProfile = instructorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + userId));
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
        return userMapper.toInstructorProfileResponse(instructorProfile, userProfile);
    }
}
