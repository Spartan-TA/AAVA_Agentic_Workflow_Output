package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    private final List<String> exports = new ArrayList<>();

    @PreAuthorize("hasAuthority('PAYROLL_EXPORT')")
    @PostMapping("/exports")
    public String generateExport(@RequestParam String period) {
        String export = "PayrollExport_" + period + ".csv";
        exports.add(export);
        return export;
    }

    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    @GetMapping("/exports")
    public List<String> getExports() {
        return Collections.unmodifiableList(exports);
    }
}
