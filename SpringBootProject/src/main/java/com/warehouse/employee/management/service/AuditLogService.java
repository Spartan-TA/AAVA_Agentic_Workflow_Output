package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.AuditLog;
import com.warehouse.employee.management.repository.AuditLogRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing AuditLog entities and exporting audit trails.
 */
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get all audit logs.
     * @return List of audit logs
     */
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Get audit log by ID.
     * @param id AuditLog ID
     * @return AuditLog entity
     */
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id: " + id));
    }

    /**
     * Create a new audit log entry.
     * @param auditLog AuditLog entity
     * @return Created audit log
     */
    @Transactional
    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    /**
     * Delete an audit log by ID.
     * @param id AuditLog ID
     */
    @Transactional
    public void deleteAuditLog(Long id) {
        AuditLog auditLog = getAuditLogById(id);
        auditLogRepository.delete(auditLog);
    }

    /**
     * Export audit logs as CSV.
     * @return CSV string of audit logs
     */
    public String exportAuditLogsAsCsv() {
        List<AuditLog> logs = getAllAuditLogs();
        StringBuilder sb = new StringBuilder();
        sb.append("id,action,entity,entityId,createdAt,createdBy
");
        for (AuditLog log : logs) {
            sb.append(log.getId()).append(",")
              .append(log.getAction()).append(",")
              .append(log.getEntity()).append(",")
              .append(log.getEntityId()).append(",")
              .append(log.getCreatedAt()).append(",")
              .append(log.getCreatedBy()).append("
");
        }
        return sb.toString();
    }
}
