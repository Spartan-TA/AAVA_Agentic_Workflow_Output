package com.warehouse.employee.service;

import com.warehouse.employee.domain.AuditLog;
import com.warehouse.employee.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for audit logging (create, update, delete).
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log a create action.
     * @param entityName Entity name
     * @param entityId Entity ID
     * @param username Username
     */
    @Transactional
    public void logCreate(String entityName, Long entityId, String username) {
        AuditLog log = new AuditLog();
        log.setAction("CREATE");
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setUsername(username);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    /**
     * Log an update action.
     * @param entityName Entity name
     * @param entityId Entity ID
     * @param username Username
     */
    @Transactional
    public void logUpdate(String entityName, Long entityId, String username) {
        AuditLog log = new AuditLog();
        log.setAction("UPDATE");
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setUsername(username);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    /**
     * Log a delete action.
     * @param entityName Entity name
     * @param entityId Entity ID
     * @param username Username
     */
    @Transactional
    public void logDelete(String entityName, Long entityId, String username) {
        AuditLog log = new AuditLog();
        log.setAction("DELETE");
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setUsername(username);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
