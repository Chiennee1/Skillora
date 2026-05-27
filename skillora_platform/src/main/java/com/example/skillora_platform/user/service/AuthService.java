package com.example.skillora_platform.user.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.dto.AuthResponse;
import com.example.skillora_platform.user.dto.LoginRequest;
import com.example.skillora_platform.user.dto.RegisterRequest;
import com.example.skillora_platform.user.dto.UserResponse;
import com.example.skillora_platform.user.entity.InstructorProfile;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserProfile;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }

        RoleName roleName = request.getRole() == null ? RoleName.STUDENT : request.getRole();
        validateSelfRegistrationRole(roleName);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        User savedUser = userRepository.save(user);

        userProfileRepository.save(UserProfile.builder()
                .user(savedUser)
                .build());

        if (roleName == RoleName.INSTRUCTOR) {
            instructorProfileRepository.save(InstructorProfile.builder()
                    .user(savedUser)
                    .title(trimToNull(request.getInstructorTitle()))
                    .expertise(trimToNull(request.getInstructorExpertise()))
                    .verified(false)
                    .build());
        }

        return issueTokens(savedUser, httpRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> invalidCredentials());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        requireActiveAccount(user);
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return issueTokens(savedUser, httpRequest);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken, HttpServletRequest httpRequest) {
        RefreshTokenService.RefreshTokenIssue issue = refreshTokenService.rotate(refreshToken, httpRequest);
        User user = issue.refreshToken().getUser();
        requireActiveAccount(user);
        return buildAuthResponse(user, issue.rawToken());
    }

    @Transactional
    public void logout(String refreshToken, HttpServletRequest httpRequest) {
        refreshTokenService.revoke(refreshToken, httpRequest);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = getActiveUserByEmail(email);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public AuthResponse issueTokens(User user, HttpServletRequest httpRequest) {
        RefreshTokenService.RefreshTokenIssue refreshTokenIssue = refreshTokenService.issue(user, httpRequest);
        return buildAuthResponse(user, refreshTokenIssue.rawToken());
    }

    @Transactional(readOnly = true)
    public User getActiveUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        requireActiveAccount(user);
        return user;
    }

    public void requireActiveAccount(User user) {
        if (user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtService.accessTokenExpiresInSeconds())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    private void validateSelfRegistrationRole(RoleName roleName) {
        if (roleName == RoleName.ADMIN) {
            throw new BusinessException("ADMIN registration is not allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private BusinessException invalidCredentials() {
        return new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
