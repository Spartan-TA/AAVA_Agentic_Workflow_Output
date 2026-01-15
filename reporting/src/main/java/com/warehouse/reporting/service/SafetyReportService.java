package com.warehouse.reporting.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SafetyReportService {
    public List<String[]> getSafetyData(String filter) {
        // Simulate safety data
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"IncidentId", "Date", "Severity", "Status"});
        data.add(new String[]{"1001", "2024-06-01", "HIGH", "RESOLVED"});
        data.add(new String[]{"1002", "2024-06-02", "LOW", "REPORTED"});
        if (filter != null && !filter.isEmpty()) {
            data.removeIf(row -> !String.join(",", row).contains(filter));
        }
        return data;
    }
}
