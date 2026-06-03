package com.example.skillora_platform.notification.controller;

import jakarta.validation.constraints.Positive;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.notification.dto.NotificationResponse;
import com.example.skillora_platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.NOTIFICATION_API_PREFIX)
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        return ApiResponse.success(notificationService.list(jwt.getSubject(), unreadOnly, page, size));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(
            @PathVariable("id") @Positive Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success("Notification marked as read",
                notificationService.markRead(id, jwt.getSubject()));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Long> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Notifications marked as read",
                notificationService.markAllRead(jwt.getSubject()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.subscribe(jwt.getSubject()).emitter();
    }
}
