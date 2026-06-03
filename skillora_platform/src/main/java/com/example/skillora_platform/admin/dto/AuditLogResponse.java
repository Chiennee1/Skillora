package com.example.skillora_platform.admin.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuditLogResponse {

    private Long id;
    private Long actorId;
    private String actorEmail;
    private String entityType;
    private Long entityId;
    private String action;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private LocalDateTime createdAt;
}
