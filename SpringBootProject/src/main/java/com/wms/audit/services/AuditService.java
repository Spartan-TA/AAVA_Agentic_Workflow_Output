package com.wms.audit.services;

import com.wms.audit.model.AuditLog;
import com.wms.audit.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Service for logging changes for audit and compliance.
 */
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    /**
     * Logs a change to an entity.
     * @param entity Entity name
     * @param id Entity ID
     * @param action Action performed
     * @param actor Actor (username or system)
     * @param before State before change (object serialized as JSON)
     * @param after State after change (object serialized as JSON)
     */
    @Transactional
    public void logChange(String entity, Long id, String action, String actor, Object before, Object after) {
        AuditLog log = new AuditLog();
        log.setEntity(entity);
        log.setEntityId(id);
        log.setAction(action);
        log.setActor(actor);
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(before != null ? before.toString() : null);
        log.setAfterState(after != null ? after.toString() : null);
        auditLogRepository.save(log);
    }
}
