package com.example.mcqassessment.controller;

import com.example.mcqassessment.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    @Autowired
    private final ExportService exportService;

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER')")
    @GetMapping("/assessment/{id}/csv")
    public ResponseEntity<byte[]> exportResultsToCSV(@PathVariable Long id) throws Exception {
        byte[] csv = exportService.exportResultsToCSV(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER')")
    @GetMapping("/assessment/{id}/pdf")
    public ResponseEntity<byte[]> exportResultsToPDF(@PathVariable Long id) throws Exception {
        byte[] pdf = exportService.exportResultsToPDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
