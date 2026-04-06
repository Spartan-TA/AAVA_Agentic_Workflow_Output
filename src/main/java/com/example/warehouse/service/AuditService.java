package com.example.warehouse.service;

import com.example.warehouse.dto.AuditLogDTO;
import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog logAction(String actor, String entity, String action, String beforeState, String afterState) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setEntity(entity);
        log.setAction(action);
        log.setBeforeState(beforeState);
        log.setAfterState(afterState);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
        return log;
    }

    public List<AuditLog> exportLogs(LocalDateTime from, LocalDateTime to, String user, String entity) {
        // Filtering logic
        return auditLogRepository.findByTimestampBetweenAndActorAndEntity(from, to, user, entity);
    }
}
