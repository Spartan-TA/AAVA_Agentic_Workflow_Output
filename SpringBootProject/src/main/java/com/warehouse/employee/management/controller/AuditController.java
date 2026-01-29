package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/audit/logs")
public class AuditController {
    private final List<String> logs = new ArrayList<>();

    @PreAuthorize("hasAuthority('AUDIT_LOG')")
    @PostMapping
    public void logChange(@RequestParam String entity, @RequestParam String changeType, @RequestParam String details) {
        String log = String.format("%s: %s - %s at %s", entity, changeType, details, new Date());
        logs.add(log);
    }

    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @GetMapping
    public List<String> getLogs(@RequestParam(required = false) String entity) {
        if (entity == null) return Collections.unmodifiableList(logs);
        List<String> filtered = new ArrayList<>();
        for (String log : logs) {
            if (log.startsWith(entity + ":")) {
                filtered.add(log);
            }
        }
        return filtered;
    }
}
