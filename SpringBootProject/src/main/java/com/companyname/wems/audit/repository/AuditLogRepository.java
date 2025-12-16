package com.companyname.wems.audit.repository;

import com.companyname.wems.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}