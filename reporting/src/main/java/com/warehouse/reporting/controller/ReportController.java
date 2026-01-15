package com.warehouse.reporting.controller;

import com.warehouse.reporting.service.ReportService;
import com.warehouse.reporting.service.AttendanceReportService;
import com.warehouse.reporting.service.SafetyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;
    @Autowired
    private AttendanceReportService attendanceReportService;
    @Autowired
    private SafetyReportService safetyReportService;

    @GetMapping("/attendance")
    public ResponseEntity<byte[]> getAttendanceReport(@RequestParam(required = false) String filter) {
        List<String[]> data = attendanceReportService.getAttendanceData(filter);
        byte[] csv = reportService.exportToCSV(data).getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "attendance-report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/safety")
    public ResponseEntity<byte[]> getSafetyReport(@RequestParam(required = false) String filter) {
        List<String[]> data = safetyReportService.getSafetyData(filter);
        byte[] csv = reportService.exportToCSV(data).getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "safety-report.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/attendance/pdf")
    public ResponseEntity<byte[]> getAttendanceReportPDF(@RequestParam(required = false) String filter) {
        List<String[]> data = attendanceReportService.getAttendanceData(filter);
        byte[] pdf = reportService.exportToPDF(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "attendance-report.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/safety/pdf")
    public ResponseEntity<byte[]> getSafetyReportPDF(@RequestParam(required = false) String filter) {
        List<String[]> data = safetyReportService.getSafetyData(filter);
        byte[] pdf = reportService.exportToPDF(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "safety-report.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
