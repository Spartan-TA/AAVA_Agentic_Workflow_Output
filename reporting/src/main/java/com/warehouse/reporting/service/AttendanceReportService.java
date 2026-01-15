package com.warehouse.reporting.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceReportService {
    public List<String[]> getAttendanceData(String filter) {
        // Simulate attendance data
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"EmployeeId", "Date", "Status"});
        data.add(new String[]{"1", "2024-06-01", "Present"});
        data.add(new String[]{"2", "2024-06-01", "Absent"});
        if (filter != null && !filter.isEmpty()) {
            data.removeIf(row -> !String.join(",", row).contains(filter));
        }
        return data;
    }
}
