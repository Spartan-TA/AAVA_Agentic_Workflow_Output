package com.example.warehouse.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/employee")
    public ResponseEntity<String> employeeReport() {
        return ResponseEntity.ok(reportService.generateEmployeeReport());
    }

    @GetMapping("/attendance")
    public ResponseEntity<String> attendanceReport() {
        return ResponseEntity.ok(reportService.generateAttendanceReport());
    }

    @GetMapping("/payroll")
    public ResponseEntity<String> payrollReport() {
        return ResponseEntity.ok(reportService.generatePayrollReport());
    }
}
