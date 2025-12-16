package com.companyname.wems.payroll.controller;

import com.companyname.wems.payroll.service.PayrollExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollExportService payrollExportService;

    // Generate payroll export
    @PostMapping("/export")
    public ResponseEntity<String> exportPayroll(@RequestBody List<Map<String, Object>> payrollData, @RequestParam String format) {
        String fileContent = payrollExportService.generatePayrollExport(payrollData, format);
        return ResponseEntity.ok(fileContent);
    }

    // Deliver payroll export
    @PostMapping("/deliver")
    public ResponseEntity<Boolean> deliverPayroll(@RequestBody String fileContent, @RequestParam String destination) {
        boolean success = payrollExportService.deliverPayrollExport(fileContent, destination);
        return ResponseEntity.ok(success);
    }

    // Audit log export action
    @PostMapping("/audit")
    public ResponseEntity<Void> auditExport(@RequestParam String userId, @RequestParam String action, @RequestParam String details) {
        payrollExportService.logExportAction(userId, action, details);
        return ResponseEntity.noContent().build();
    }
}