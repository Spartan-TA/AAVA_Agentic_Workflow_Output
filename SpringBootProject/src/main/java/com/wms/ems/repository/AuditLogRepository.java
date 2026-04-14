package com.wms.ems.repository;

import com.wms.ems.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity operations.
 * Provides CRUD and custom query methods for audit log management.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * Find audit logs by entity type and entity ID.
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return List of AuditLogs
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find audit logs by actor and timestamp range.
     * @param actor the actor
     * @param start start timestamp
     * @param end end timestamp
     * @return List of AuditLogs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.actor = :actor AND a.timestamp >= :start AND a.timestamp <= :end")
    List<AuditLog> findByActorAndTimestampBetween(@Param("actor") String actor, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
