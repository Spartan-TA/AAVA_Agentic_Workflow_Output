package com.companyname.wems.audit.controller;

import com.companyname.wems.audit.model.AuditLog;
import com.companyname.wems.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    // Get logs by entity type
    @GetMapping("/entity/{type}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(@PathVariable String type) {
        return ResponseEntity.ok(auditService.getLogsByEntityType(type));
    }

    // Get logs by user
    @GetMapping("/user/{id}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getLogsByUserId(id));
    }

    // Get logs by date range
    @GetMapping("/date")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(@RequestParam LocalDateTime start, @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(auditService.getLogsByDateRange(start, end));
    }
}