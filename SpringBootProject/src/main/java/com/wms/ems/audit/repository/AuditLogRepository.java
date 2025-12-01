package com.wms.ems.audit.repository;

import com.wms.ems.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Repository interface for AuditLog entity.
 * Provides CRUD operations and custom queries for audit logs.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * Find all audit logs for a specific user.
     * @param username the username
     * @return List of AuditLog
     */
    List<AuditLog> findByUsername(String username);

    /**
     * Find all audit logs after a specific timestamp.
     * @param timestamp the timestamp
     * @return List of AuditLog
     */
    List<AuditLog> findByTimestampAfter(LocalDateTime timestamp);
}
