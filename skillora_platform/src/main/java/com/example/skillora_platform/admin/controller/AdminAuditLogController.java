package com.example.skillora_platform.admin.controller;

import java.time.LocalDateTime;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.admin.dto.AuditLogResponse;
import com.example.skillora_platform.admin.service.AdminAuditLogService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping(Constants.ADMIN_API_PREFIX + "/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AuditLogResponse>> listAuditLogs(
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "actorId", required = false) Long actorId,
            @RequestParam(name = "from", required = false) LocalDateTime from,
            @RequestParam(name = "to", required = false) LocalDateTime to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {

        return ApiResponse.success(adminAuditLogService.listAuditLogs(
                entityType, action, actorId, from, to, page, size));
    }
}
