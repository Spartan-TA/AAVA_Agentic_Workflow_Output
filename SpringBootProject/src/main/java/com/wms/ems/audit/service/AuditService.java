package com.wms.ems.audit.service;

import com.wms.ems.audit.repository.AuditLogRepository;
import com.wms.ems.audit.entity.AuditLog;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit logs.
 */
@Service
@Transactional
@Slf4j
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Logs an action as an immutable audit log.
     * @param entity Entity name
     * @param entityId Entity ID
     * @param action Action performed
     * @param actor Actor name
     * @param beforeState State before action
     * @param afterState State after action
     */
    public void logAction(String entity, Long entityId, String action, String actor, String beforeState, String afterState) {
        if (entity == null || entity.isEmpty() || entityId == null || action == null || action.isEmpty() || actor == null || actor.isEmpty()) {
            log.error("Validation failed: All fields are required");
            throw new ValidationException("All fields are required");
        }
        AuditLog logEntry = new AuditLog();
        logEntry.setEntity(entity);
        logEntry.setEntityId(entityId);
        logEntry.setAction(action);
        logEntry.setActor(actor);
        logEntry.setBeforeState(beforeState);
        logEntry.setAfterState(afterState);
        logEntry.setTimestamp(LocalDateTime.now());
        try {
            auditLogRepository.save(logEntry);
            log.info("Audit log created for {} {} by {}", entity, entityId, actor);
        } catch (Exception e) {
            log.error("Failed to log action", e);
            throw new BusinessException("Failed to log action");
        }
    }

    /**
     * Gets audit logs for an entity and entity ID.
     * @param entity Entity name
     * @param entityId Entity ID
     * @return List of AuditLog
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(String entity, Long entityId) {
        try {
            return auditLogRepository.findByEntityAndEntityId(entity, entityId);
        } catch (Exception e) {
            log.error("Failed to fetch audit logs", e);
            throw new BusinessException("Failed to fetch audit logs");
        }
    }

    /**
     * Gets audit logs by actor and date range.
     * @param actor Actor name
     * @param start Start date
     * @param end End date
     * @return List of AuditLog
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByActor(String actor, LocalDateTime start, LocalDateTime end) {
        if (actor == null || actor.isEmpty() || start == null || end == null || end.isBefore(start)) {
            log.error("Invalid parameters for audit log query");
            throw new ValidationException("Invalid parameters");
        }
        try {
            return auditLogRepository.findByActorAndTimestampBetween(actor, start, end);
        } catch (Exception e) {
            log.error("Failed to fetch audit logs by actor", e);
            throw new BusinessException("Failed to fetch audit logs by actor");
        }
    }

    /**
     * Gets audit logs by date range.
     * @param start Start date
     * @param end End date
     * @return List of AuditLog
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            log.error("Invalid date range for audit logs");
            throw new ValidationException("Invalid date range");
        }
        try {
            return auditLogRepository.findByTimestampBetween(start, end);
        } catch (Exception e) {
            log.error("Failed to fetch audit logs by date range", e);
            throw new BusinessException("Failed to fetch audit logs by date range");
        }
    }
}
