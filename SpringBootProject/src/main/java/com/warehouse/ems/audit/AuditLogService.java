package com.warehouse.ems.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for logging sensitive operations to the audit trail.
 */
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log an action with before/after state as JSON.
     */
    @Transactional
    public void log(String actor, String entity, Long entityId, AuditLog.Action action, Object before, Object after) {
        try {
            AuditLog log = new AuditLog();
            log.setActor(actor);
            log.setTimestamp(LocalDateTime.now());
            log.setEntity(entity);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setBefore(before != null ? objectMapper.writeValueAsString(before) : null);
            log.setAfter(after != null ? objectMapper.writeValueAsString(after) : null);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // In production, handle/log serialization errors
            throw new RuntimeException("Failed to log audit entry", e);
        }
    }
}
