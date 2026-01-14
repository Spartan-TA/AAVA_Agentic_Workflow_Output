package com.example.warehouse.service;

import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for AuditLog operations.
 */
@Service
public class AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> getLogsByEntityTypeAndDateRange(String entityType, LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByEntityTypeAndDateRange(entityType, from, to);
    }

    public List<AuditLog> getLogsByActor(String actor) {
        return auditLogRepository.findByActor(actor);
    }

    @Transactional
    public AuditLog logChange(AuditLog log) {
        log.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(log);
    }
}
