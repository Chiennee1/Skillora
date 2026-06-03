package com.example.skillora_platform.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.dto.AdminUserResponse;
import com.example.skillora_platform.admin.dto.AdminUserStatusRequest;
import com.example.skillora_platform.admin.spec.AdminUserSpec;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> listUsers(UserStatus status, String role,
                                                      String search, int page, int size) {
        Specification<User> spec = Specification.where(AdminUserSpec.notDeleted())
                .and(AdminUserSpec.statusEquals(status))
                .and(AdminUserSpec.hasRole(role))
                .and(AdminUserSpec.searchByNameOrEmail(search));

        Page<AdminUserResponse> result = userRepository.findAll(spec,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse updateStatus(Long id, AdminUserStatusRequest request,
                                           String adminEmail, String ipAddress) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new BusinessException("Cannot change your own status", HttpStatus.BAD_REQUEST);
        }

        UserStatus oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        userRepository.save(user);

        auditLogService.log(adminEmail, "USER", id, "CHANGE_STATUS",
                "{\"status\":\"" + oldStatus + "\"}",
                "{\"status\":\"" + request.getStatus() + "\"}",
                ipAddress, null);

        log.info("Admin {} changed user {} status from {} to {}",
                adminEmail, id, oldStatus, request.getStatus());

        return toResponse(user);
    }

    private AdminUserResponse toResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .roles(roles)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
