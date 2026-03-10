package com.example.warehouse.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditService {
    @Autowired
    private AuditRepository auditRepository;

    public List<AuditLog> getAllLogs() {
        return auditRepository.findAll();
    }

    public Optional<AuditLog> getLogById(Long id) {
        return auditRepository.findById(id);
    }

    public AuditLog logAction(String action, String entity, Long entityId, String performedBy) {
        AuditLog log = new AuditLog(action, entity, entityId, performedBy, LocalDateTime.now());
        return auditRepository.save(log);
    }
}
