package com.example.skillora_platform.admin.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.admin.dto.AuditLogResponse;
import com.example.skillora_platform.admin.entity.AuditLog;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.admin.spec.AuditLogSpec;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

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

        Specification<AuditLog> spec = Specification.where(AuditLogSpec.entityTypeEquals(entityType))
                .and(AuditLogSpec.actionEquals(action))
                .and(AuditLogSpec.actorIdEquals(actorId))
                .and(AuditLogSpec.createdAfter(from))
                .and(AuditLogSpec.createdBefore(to));

        Page<AuditLogResponse> result = auditLogRepository.findAll(spec,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);

        return ApiResponse.success(PageResponse.from(result));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActor() != null ? log.getActor().getId() : null)
                .actorEmail(log.getActor() != null ? log.getActor().getEmail() : null)
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValues(log.getOldValues())
                .newValues(log.getNewValues())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
