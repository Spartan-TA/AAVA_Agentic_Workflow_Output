package com.warehouse.ems.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditLog entity. Table should be immutable (no update/delete).
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // No update/delete methods exposed for immutability
}
