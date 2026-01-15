package com.warehouse.audit.service;

import com.warehouse.audit.entity.AuditLog;
import com.warehouse.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByEntity(String entity) {
        return auditLogRepository.findByEntity(entity);
    }

    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    public String exportLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,action,entity,entityId,username,timestamp,details
");
        for (AuditLog log : logs) {
            sb.append(log.getId()).append(",")
              .append(log.getAction()).append(",")
              .append(log.getEntity()).append(",")
              .append(log.getEntityId()).append(",")
              .append(log.getUsername()).append(",")
              .append(log.getTimestamp()).append(",")
              .append(log.getDetails().replaceAll(",", ";")).append("
");
        }
        return sb.toString();
    }
}
