package com.companyname.wems.audit.service;

import com.companyname.wems.audit.model.AuditLog;
import com.companyname.wems.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    // Log create/update/delete operations
    public AuditLog logAction(String entityType, Long entityId, String action, Long userId, String beforeValue, String afterValue) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .userId(userId)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .build();
        return auditLogRepository.save(log);
    }

    // Query audit logs with filters
    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }

    public List<AuditLog> getLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }
}