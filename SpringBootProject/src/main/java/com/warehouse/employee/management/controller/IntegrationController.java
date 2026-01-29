package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class IntegrationController {
    private final List<String> logs = new ArrayList<>();

    @PreAuthorize("hasAuthority('INTEGRATION_HRIS')")
    @PostMapping("/hris")
    public String syncHris() {
        String log = "HRIS sync at " + new Date();
        logs.add(log);
        return log;
    }

    @PreAuthorize("hasAuthority('INTEGRATION_WMS')")
    @PostMapping("/wms")
    public String syncWms() {
        String log = "WMS sync at " + new Date();
        logs.add(log);
        return log;
    }

    @PreAuthorize("hasAuthority('INTEGRATION_WEBHOOK')")
    @PostMapping("/webhooks")
    public String handleWebhook(@RequestBody String payload) {
        String log = "Webhook: " + payload;
        logs.add(log);
        return log;
    }

    @PreAuthorize("hasAuthority('INTEGRATION_READ')")
    @GetMapping("/logs")
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
