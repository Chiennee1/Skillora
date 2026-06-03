package com.example.skillora_platform.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.entity.AuditLog;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Creates an audit log entry for an admin action.
     *
     * @param actorEmail  email of the admin performing the action
     * @param entityType  type of the entity being modified (e.g., "USER", "COURSE", "COUPON")
     * @param entityId    ID of the entity being modified (nullable)
     * @param action      action performed (e.g., "BAN_USER", "APPROVE_COURSE")
     * @param oldValues   JSON string of old values (nullable)
     * @param newValues   JSON string of new values (nullable)
     * @param ipAddress   IP address of the request (nullable)
     * @param userAgent   user agent of the request (nullable)
     */
    @Transactional
    public void log(String actorEmail, String entityType, Long entityId, String action,
                    String oldValues, String newValues, String ipAddress, String userAgent) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .oldValues(oldValues)
                .newValues(newValues)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log created: actor={}, action={}, entity={}#{}", actorEmail, action, entityType, entityId);
    }
}
