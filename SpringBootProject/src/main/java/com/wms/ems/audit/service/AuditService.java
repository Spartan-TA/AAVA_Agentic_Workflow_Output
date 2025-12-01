package com.wms.ems.audit.service;

import com.wms.ems.audit.entity.AuditLog;
import com.wms.ems.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Audit management.
 * Handles centralized logging.
 */
@Service
@Transactional
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log an audit event.
     * @param log the audit log
     * @return the saved AuditLog
     */
    public AuditLog logEvent(AuditLog log) {
        log.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    /**
     * Get all audit logs for a user.
     * @param username the username
     * @return List of AuditLog
     */
    public List<AuditLog> getLogsForUser(String username) {
        return auditLogRepository.findByUsername(username);
    }

    /**
     * Get all audit logs after a timestamp.
     * @param timestamp the timestamp
     * @return List of AuditLog
     */
    public List<AuditLog> getLogsAfter(LocalDateTime timestamp) {
        return auditLogRepository.findByTimestampAfter(timestamp);
    }
}
