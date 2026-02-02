package com.wms.audit.repositories;

import com.wms.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Additional query methods can be defined here if needed
}
