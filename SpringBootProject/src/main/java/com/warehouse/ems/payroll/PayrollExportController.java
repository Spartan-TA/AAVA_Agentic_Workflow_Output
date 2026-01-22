package com.warehouse.ems.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for Payroll export endpoint.
 */
@RestController
@RequestMapping("/payroll/export")
@Validated
public class PayrollExportController {
    private final PayrollExportService exportService;

    @Autowired
    public PayrollExportController(PayrollExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Export payroll data for a date range.
     */
    @PostMapping
    public ResponseEntity<String> exportPayroll(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                               @RequestParam(defaultValue = "CSV") String format) {
        try {
            String result = exportService.exportPayroll(start, end, format);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return new ResponseEntity<>("Export failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
