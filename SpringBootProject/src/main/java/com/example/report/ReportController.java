package com.example.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/{type}")
    public String generateReport(@PathVariable String type) {
        return reportService.generateReport(type);
    }
}