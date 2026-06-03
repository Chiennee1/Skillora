package com.example.skillora_platform.notification.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.notification.dto.NotificationResponse;
import com.example.skillora_platform.notification.entity.Notification;
import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.NotificationCreatedEvent;
import com.example.skillora_platform.notification.repository.NotificationRepository;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CoursePermissionService permissionService;
    private final ObjectMapper objectMapper;
    private final NotificationStreamService streamService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(
            String actorEmail,
            boolean unreadOnly,
            int page,
            int size
    ) {
        User actor = permissionService.requireActor(actorEmail);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size));
        Page<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(actor.getId(), pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(actor.getId(), pageable);
        return PageResponse.from(notifications.map(this::toResponse));
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public long markAllRead(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        LocalDateTime readAt = LocalDateTime.now();
        var unreadNotifications = notificationRepository.findByUserIdAndReadAtIsNull(actor.getId());
        unreadNotifications.forEach(notification -> notification.setReadAt(readAt));
        notificationRepository.saveAll(unreadNotifications);
        return unreadNotifications.size();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        return notificationRepository.countByUserIdAndReadAtIsNull(actor.getId());
    }

    @Transactional
    public NotificationResponse createNotification(
            Long userId,
            NotificationType type,
            String title,
            String content,
            Map<String, Object> data
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .data(toJson(data))
                .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);
        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId(), userId));
        log.debug("Created notification {} for user {}", response.getId(), userId);
        return response;
    }

    @Transactional(readOnly = true)
    public NotificationResponse getResponseForStream(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public SseSubscription subscribe(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        return new SseSubscription(actor.getId(), streamService.subscribe(actor.getId()));
    }

    private String toJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Notification data must be JSON serializable", ex);
        }
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
    }

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .data(toJsonNode(notification.getData()))
                .read(notification.getReadAt() != null)
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private JsonNode toJsonNode(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(data);
        } catch (JsonProcessingException ex) {
            log.warn("Notification data is not valid JSON: {}", data);
            return null;
        }
    }
}
