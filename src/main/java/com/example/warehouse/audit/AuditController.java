package com.example.warehouse.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    public List<AuditLog> getAllLogs() {
        return auditService.getAllLogs();
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<AuditLog> getLogById(@PathVariable Long id) {
        return auditService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/logs")
    public AuditLog logAction(@RequestParam String action, @RequestParam String entity, @RequestParam Long entityId, @RequestParam String performedBy) {
        return auditService.logAction(action, entity, entityId, performedBy);
    }
}
