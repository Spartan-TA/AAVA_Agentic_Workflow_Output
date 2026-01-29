package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    private final List<String> logs = new ArrayList<>();

    @PreAuthorize("hasAuthority('ONBOARDING_PROVISION')")
    @PostMapping("/provision")
    public String provisionEmployee(@RequestParam Long employeeId) {
        String log = "Provisioned employee: " + employeeId + " at " + new Date();
        logs.add(log);
        return log;
    }

    @PreAuthorize("hasAuthority('ONBOARDING_DEPROVISION')")
    @PostMapping("/deprovision")
    public String deprovisionEmployee(@RequestParam Long employeeId) {
        String log = "Deprovisioned employee: " + employeeId + " at " + new Date();
        logs.add(log);
        return log;
    }

    @PreAuthorize("hasAuthority('ONBOARDING_READ')")
    @GetMapping("/logs")
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
