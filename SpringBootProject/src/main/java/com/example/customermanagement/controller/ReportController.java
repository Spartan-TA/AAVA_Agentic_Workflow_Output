package com.example.customermanagement.controller;

import com.example.customermanagement.dto.SalesReportDTO;
import com.example.customermanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for generating sales reports (admin only).
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Generate sales report (admin only).
     * @return Sales report data
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesReportDTO> getSalesReport() {
        SalesReportDTO report = reportService.generateSalesReport();
        return ResponseEntity.ok(report);
    }
}
