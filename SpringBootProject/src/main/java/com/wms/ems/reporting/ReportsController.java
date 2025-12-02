package com.wms.ems.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportingService reportingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<ReportDto>> generateReport(@RequestBody ReportFilterDto filter) {
        return ResponseEntity.ok(reportingService.generateReport(filter));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<byte[]> exportReport(@RequestBody List<ReportDto> report, @RequestParam String format) {
        byte[] file = reportingService.exportReport(report, format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(format.equalsIgnoreCase("pdf") ? MediaType.APPLICATION_PDF : MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "report." + format);
        return ResponseEntity.ok().headers(headers).body(file);
    }

    @GetMapping("/metrics/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<ReportDto> getMetrics(@PathVariable String type) {
        return ResponseEntity.ok(reportingService.getMetrics(type));
    }
}
