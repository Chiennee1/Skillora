package com.example.skillora_platform.course.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.repository.UserRepository;
import com.example.skillora_platform.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoursePermissionService {

    private final AuthService authService;
    private final UserRepository userRepository;

    public User requireInstructorOrAdmin(String actorEmail) {
        User actor = requireActor(actorEmail);
        if (!hasRole(actor, RoleName.INSTRUCTOR) && !hasRole(actor, RoleName.ADMIN)) {
            throw new BusinessException("Instructor or admin role is required", HttpStatus.FORBIDDEN);
        }
        return actor;
    }

    public User requireActor(String actorEmail) {
        if (actorEmail == null || actorEmail.isBlank()) {
            throw new BusinessException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return authService.getActiveUserByEmail(actorEmail);
    }

    public User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        authService.requireActiveAccount(user);
        return user;
    }

    public void requireOwnerOrAdmin(Course course, User actor) {
        if (isAdmin(actor)) {
            return;
        }
        if (course.getInstructor() == null || !course.getInstructor().getId().equals(actor.getId())) {
            throw new BusinessException("Course owner or admin role is required", HttpStatus.FORBIDDEN);
        }
    }

    public boolean canManage(Course course, User actor) {
        return actor != null && (isAdmin(actor)
                || course.getInstructor() != null && course.getInstructor().getId().equals(actor.getId()));
    }

    public boolean isAdmin(User user) {
        return hasRole(user, RoleName.ADMIN);
    }

    public boolean isInstructor(User user) {
        return hasRole(user, RoleName.INSTRUCTOR);
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user != null && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equals);
    }
}
