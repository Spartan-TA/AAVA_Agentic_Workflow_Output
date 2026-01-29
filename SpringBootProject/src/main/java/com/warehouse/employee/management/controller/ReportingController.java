package com.warehouse.employee.management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportingController {
    private final List<String> reports = new ArrayList<>();

    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    @PostMapping
    public String generateReport(@RequestParam String type) {
        String report = type + "_" + System.currentTimeMillis();
        reports.add(report);
        return report;
    }

    @PreAuthorize("hasAuthority('REPORT_READ')")
    @GetMapping
    public List<String> getReports() {
        return Collections.unmodifiableList(reports);
    }

    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    @GetMapping("/export/csv/{index}")
    public String exportToCsv(@PathVariable int index) {
        if (index < 0 || index >= reports.size()) throw new IllegalArgumentException("Invalid index");
        return "Exported to CSV: " + reports.get(index);
    }

    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    @GetMapping("/export/pdf/{index}")
    public String exportToPdf(@PathVariable int index) {
        if (index < 0 || index >= reports.size()) throw new IllegalArgumentException("Invalid index");
        return "Exported to PDF: " + reports.get(index);
    }
}
