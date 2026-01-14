package com.example.warehouse.repository;

import com.example.warehouse.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.timestamp BETWEEN :from AND :to")
    List<AuditLog> findByEntityTypeAndDateRange(@Param("entityType") String entityType, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AuditLog a WHERE a.actor = :actor")
    List<AuditLog> findByActor(@Param("actor") String actor);
}
