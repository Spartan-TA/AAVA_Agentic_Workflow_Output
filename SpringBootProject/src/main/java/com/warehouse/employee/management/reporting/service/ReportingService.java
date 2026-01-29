package com.warehouse.employee.management.reporting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ReportingService {
    private final List<String> reports = new ArrayList<>();

    @Transactional
    public String generateReport(String reportType) {
        String report = reportType + "_" + System.currentTimeMillis();
        reports.add(report);
        return report;
    }

    public void exportToCsv(String reportName) {
        // Stub for CSV export logic
    }

    public void exportToPdf(String reportName) {
        // Stub for PDF export logic
    }

    public List<String> getAllReports() {
        return Collections.unmodifiableList(reports);
    }
}
