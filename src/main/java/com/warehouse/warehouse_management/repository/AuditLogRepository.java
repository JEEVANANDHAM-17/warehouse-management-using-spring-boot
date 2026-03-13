package com.warehouse.warehouse_management.repository;

import com.warehouse.warehouse_management.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
