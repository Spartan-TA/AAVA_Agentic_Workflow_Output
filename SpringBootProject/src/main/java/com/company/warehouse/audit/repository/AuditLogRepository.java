package com.company.warehouse.audit.repository;

import com.company.warehouse.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}