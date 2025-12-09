package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditEntry entity.
 */
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findByEntityType(String entityType);
    List<AuditEntry> findByEntityId(String entityId);
    List<AuditEntry> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
