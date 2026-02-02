package com.wms.reporting.controllers;

import com.wms.reporting.model.Report;
import com.wms.reporting.services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reporting endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {
    private final ReportingService reportingService;

    /**
     * GET endpoint for attendance report.
     */
    @GetMapping("/attendance")
    public ResponseEntity<Report> getAttendanceReport() {
        return ResponseEntity.ok(reportingService.generateAttendanceReport());
    }

    /**
     * GET endpoint for overtime report.
     */
    @GetMapping("/overtime")
    public ResponseEntity<Report> getOvertimeReport() {
        return ResponseEntity.ok(reportingService.generateOvertimeReport());
    }

    /**
     * GET endpoint for leave balance report.
     */
    @GetMapping("/leave-balance")
    public ResponseEntity<Report> getLeaveBalanceReport() {
        return ResponseEntity.ok(reportingService.generateLeaveBalanceReport());
    }

    /**
     * GET endpoint for certification status report.
     */
    @GetMapping("/certification-status")
    public ResponseEntity<Report> getCertificationStatusReport() {
        return ResponseEntity.ok(reportingService.generateCertificationStatusReport());
    }

    /**
     * GET endpoint for safety KPI report.
     */
    @GetMapping("/safety-kpi")
    public ResponseEntity<Report> getSafetyKPIReport() {
        return ResponseEntity.ok(reportingService.generateSafetyKPIReport());
    }
}
