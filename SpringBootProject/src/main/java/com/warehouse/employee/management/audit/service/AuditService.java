package com.warehouse.employee.management.audit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AuditService {
    private final List<String> auditLogs = new ArrayList<>();

    @Transactional
    public void logChange(String entity, String changeType, String details) {
        String log = String.format("%s: %s - %s at %s", entity, changeType, details, new Date());
        auditLogs.add(log);
    }

    public List<String> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }
}
