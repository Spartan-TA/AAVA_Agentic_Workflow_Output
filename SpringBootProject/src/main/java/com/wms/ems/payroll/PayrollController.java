package com.wms.ems.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollExportService payrollExportService;

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> exportPayroll(@RequestBody PayrollExportRequestDto request) {
        PayrollFile file = payrollExportService.generatePayrollFile(request);
        payrollExportService.deliverPayrollFile(file, 3);
        return ResponseEntity.ok("Payroll export initiated");
    }
}
