package com.example.report;

import org.springframework.stereotype.Service;

@Service
public class ReportService {
    public String generateReport(String type) {
        // TODO: Implement report generation logic
        return "Report generated for type: " + type;
    }
}