package com.example.ems.controller;

import com.example.ems.dto.ReportDto;
import com.example.ems.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping
    public ResponseEntity<ReportDto> createReport(@Valid @RequestBody ReportDto reportDto) {
        return ResponseEntity.ok(reportService.createReport(reportDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportDto> updateReport(@PathVariable Long id, @Valid @RequestBody ReportDto reportDto) {
        return ResponseEntity.ok(reportService.updateReport(id, reportDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}