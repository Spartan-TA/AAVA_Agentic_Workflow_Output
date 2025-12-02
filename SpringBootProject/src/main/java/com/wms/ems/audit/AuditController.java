package com.wms.ems.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<List<AuditLog>> getLogs(@RequestParam(required = false) String actor, @RequestParam(required = false) String entity) {
        // Filtering logic can be added here
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<List<AuditLog>> exportLogs(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditService.exportLogs(start, end));
    }
}
