package com.example.warehouse.payroll.controller;

import com.example.warehouse.payroll.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {
    @Autowired
    private PayrollService payrollService;

    // Calculate payroll for an employee for a given period
    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePayroll(
            @RequestParam Long employeeId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd) {
        Map<String, Object> payroll = payrollService.calculatePayroll(employeeId, periodStart, periodEnd);
        return ResponseEntity.ok(payroll);
    }
}
