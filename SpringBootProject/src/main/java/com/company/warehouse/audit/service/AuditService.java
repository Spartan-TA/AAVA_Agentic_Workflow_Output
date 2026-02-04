package com.company.warehouse.audit.service;

import com.company.warehouse.audit.entity.AuditLog;
import com.company.warehouse.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for audit logging.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String actor, String entity, String action, String before, String after) {
        AuditLog log = AuditLog.builder()
                .actor(actor)
                .entity(entity)
                .action(action)
                .before(before)
                .after(after)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}