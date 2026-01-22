package com.warehouse.ems.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for reporting endpoints.
 */
@RestController
@RequestMapping("/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<ReportDto>> getAttendanceReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String shift,
            @RequestParam(defaultValue = "CSV") String format) {
        return ResponseEntity.ok(reportService.getAttendanceReports(from, to, department, shift));
    }

    @GetMapping("/overtime")
    public ResponseEntity<List<ReportDto>> getOvertimeReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String shift,
            @RequestParam(defaultValue = "CSV") String format) {
        return ResponseEntity.ok(reportService.getOvertimeReports(from, to, department, shift));
    }

    @GetMapping("/leave")
    public ResponseEntity<List<ReportDto>> getLeaveReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "CSV") String format) {
        return ResponseEntity.ok(reportService.getLeaveReports(from, to, department));
    }

    @GetMapping("/certifications")
    public ResponseEntity<List<ReportDto>> getCertificationReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "CSV") String format) {
        return ResponseEntity.ok(reportService.getCertificationReports(from, to, department));
    }

    @GetMapping("/safety")
    public ResponseEntity<List<ReportDto>> getSafetyReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "CSV") String format) {
        return ResponseEntity.ok(reportService.getSafetyReports(from, to, department));
    }
}
