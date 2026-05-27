package com.example.skillora_platform.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.dto.ProfileResponse;
import com.example.skillora_platform.user.dto.ProfileUpdateRequest;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserProfile;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthService authService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String email) {
        User user = authService.getActiveUserByEmail(email);
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        return userMapper.toProfileResponse(user, profile);
    }

    @Transactional
    public ProfileResponse updateMyProfile(String email, ProfileUpdateRequest request) {
        User user = authService.getActiveUserByEmail(email);
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        }
        profile.setPhone(trimToNull(request.getPhone()));
        profile.setHeadline(trimToNull(request.getHeadline()));
        profile.setBio(trimToNull(request.getBio()));
        profile.setWebsite(trimToNull(request.getWebsite()));
        profile.setLocation(trimToNull(request.getLocation()));

        User savedUser = userRepository.save(user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        return userMapper.toProfileResponse(savedUser, savedProfile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        return userMapper.toProfileResponse(user, profile);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
