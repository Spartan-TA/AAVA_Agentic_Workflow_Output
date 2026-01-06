package com.example.warehouse.service;

import com.example.warehouse.dto.AuditLogDTO;
import com.example.warehouse.entity.AuditLog;
import com.example.warehouse.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing audit logs.
 */
@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Record an audit log entry.
     * @param action Action performed
     * @param username Username
     * @param details Details
     * @return AuditLogDTO
     */
    @Transactional
    public AuditLogDTO recordAudit(String action, String username, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
        return AuditLogDTO.fromEntity(log);
    }

    /**
     * Get all audit logs.
     * @return List of AuditLogDTO
     */
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAllAuditLogs() {
        return auditLogRepository.findAll().stream()
                .map(AuditLogDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
