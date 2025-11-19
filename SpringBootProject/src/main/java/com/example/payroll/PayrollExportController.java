package com.example.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/payroll-export")
public class PayrollExportController {
    @Autowired
    private PayrollExportService payrollExportService;

    @PostMapping
    public String exportPayroll(@RequestBody List<String> employeeIds) {
        return payrollExportService.exportPayroll(employeeIds);
    }
}