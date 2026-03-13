package com.warehouse.warehouse_management.service;

import com.warehouse.warehouse_management.entity.AuditLog;
import com.warehouse.warehouse_management.entity.User;
import com.warehouse.warehouse_management.persistence.AuditLogPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogPersistenceService auditLogPersistenceService;

    public void log(String action, String entityType, Long entityId, String details, User actor) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .actorId(actor != null ? actor.getId() : null)
                .actorName(actor != null ? actor.getName() : "System")
                .actorEmail(actor != null ? actor.getEmail() : "system")
                .createdAt(LocalDateTime.now())
                .build();

        auditLogPersistenceService.save(auditLog);
    }
}
