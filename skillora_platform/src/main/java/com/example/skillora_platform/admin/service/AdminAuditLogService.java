package com.example.skillora_platform.admin.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.dto.AuditLogResponse;
import com.example.skillora_platform.admin.entity.AuditLog;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.admin.spec.AuditLogSpec;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAuditLogs(
            String entityType,
            String action,
            Long actorId,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        Specification<AuditLog> spec = Specification.where(AuditLogSpec.entityTypeEquals(entityType))
                .and(AuditLogSpec.actionEquals(action))
                .and(AuditLogSpec.actorIdEquals(actorId))
                .and(AuditLogSpec.createdAfter(from))
                .and(AuditLogSpec.createdBefore(to));
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, Constants.MAX_PAGE_SIZE));

        Page<AuditLogResponse> result = auditLogRepository.findAll(
                        spec,
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);
        return PageResponse.from(result);
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
