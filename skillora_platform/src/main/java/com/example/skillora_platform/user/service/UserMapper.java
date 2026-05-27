package com.example.skillora_platform.user.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.skillora_platform.user.dto.InstructorProfileResponse;
import com.example.skillora_platform.user.dto.ProfileResponse;
import com.example.skillora_platform.user.dto.UserResponse;
import com.example.skillora_platform.user.entity.InstructorProfile;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserProfile;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(toRoleNames(user))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ProfileResponse toProfileResponse(User user, UserProfile profile) {
        return ProfileResponse.builder()
                .id(profile == null ? null : profile.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .phone(profile == null ? null : profile.getPhone())
                .headline(profile == null ? null : profile.getHeadline())
                .bio(profile == null ? null : profile.getBio())
                .website(profile == null ? null : profile.getWebsite())
                .location(profile == null ? null : profile.getLocation())
                .createdAt(profile == null ? null : profile.getCreatedAt())
                .updatedAt(profile == null ? null : profile.getUpdatedAt())
                .build();
    }

    public InstructorProfileResponse toInstructorProfileResponse(
            InstructorProfile instructorProfile,
            UserProfile userProfile
    ) {
        User user = instructorProfile.getUser();
        return InstructorProfileResponse.builder()
                .id(instructorProfile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .title(instructorProfile.getTitle())
                .expertise(instructorProfile.getExpertise())
                .introVideoUrl(instructorProfile.getIntroVideoUrl())
                .verified(instructorProfile.isVerified())
                .headline(userProfile == null ? null : userProfile.getHeadline())
                .bio(userProfile == null ? null : userProfile.getBio())
                .website(userProfile == null ? null : userProfile.getWebsite())
                .location(userProfile == null ? null : userProfile.getLocation())
                .createdAt(instructorProfile.getCreatedAt())
                .updatedAt(instructorProfile.getUpdatedAt())
                .build();
    }

    private List<String> toRoleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
