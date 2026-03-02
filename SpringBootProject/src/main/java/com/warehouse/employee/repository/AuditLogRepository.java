package com.warehouse.employee.repository;

import com.warehouse.employee.domain.AuditLog;
import com.warehouse.employee.domain.AuditLog.Action;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * Find audit logs by entity type and entity id.
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find audit logs by actor.
     */
    List<AuditLog> findByActor(String actor);

    /**
     * Find audit logs between two timestamps.
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
