package com.warehouse.warehouse_management.persistence;

import com.warehouse.warehouse_management.entity.AuditLog;
import com.warehouse.warehouse_management.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogPersistenceService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
}
