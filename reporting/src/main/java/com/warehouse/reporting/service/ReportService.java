package com.warehouse.reporting.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {
    public String exportToCSV(List<String[]> data) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : data) {
            sb.append(String.join(",", row)).append("
");
        }
        return sb.toString();
    }

    public byte[] exportToPDF(List<String[]> data) {
        // Simulate PDF export logic
        StringBuilder sb = new StringBuilder();
        for (String[] row : data) {
            sb.append(String.join(" | ", row)).append("
");
        }
        return sb.toString().getBytes();
    }

    public List<String[]> filterData(List<String[]> data, String filter) {
        return data.stream()
                .filter(row -> String.join(",", row).contains(filter))
                .toList();
    }
}
