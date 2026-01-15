package com.warehouse.audit.repository;

import com.warehouse.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntity(String entity);
    List<AuditLog> findByUsername(String username);
}
