package com.wms.ems.audit.repository;

import com.wms.ems.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity operations.
 * Provides CRUD operations and custom queries for audit log management.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds audit logs by entity and entity ID.
     * @param entity the entity name
     * @param entityId the entity ID
     * @return a list of audit logs
     */
    List<AuditLog> findByEntityAndEntityId(String entity, Long entityId);

    /**
     * Finds audit logs by actor and timestamp range.
     * @param actor the actor name
     * @param start the start timestamp
     * @param end the end timestamp
     * @return a list of audit logs
     */
    List<AuditLog> findByActorAndTimestampBetween(String actor, LocalDateTime start, LocalDateTime end);
}
