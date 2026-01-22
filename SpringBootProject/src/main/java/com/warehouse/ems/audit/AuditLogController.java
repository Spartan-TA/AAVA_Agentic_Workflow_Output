package com.warehouse.ems.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for AuditLog endpoints.
 */
@RestController
@RequestMapping("/audit/logs")
@Validated
public class AuditLogController {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get audit logs with optional filters for date, user, and entity.
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String entity
    ) {
        List<AuditLog> logs = auditLogRepository.findAll();
        if (from != null) {
            logs = logs.stream().filter(l -> !l.getTimestamp().isBefore(from)).collect(Collectors.toList());
        }
        if (to != null) {
            logs = logs.stream().filter(l -> !l.getTimestamp().isAfter(to)).collect(Collectors.toList());
        }
        if (actor != null) {
            logs = logs.stream().filter(l -> l.getActor().equals(actor)).collect(Collectors.toList());
        }
        if (entity != null) {
            logs = logs.stream().filter(l -> l.getEntity().equals(entity)).collect(Collectors.toList());
        }
        return ResponseEntity.ok(logs);
    }
}
