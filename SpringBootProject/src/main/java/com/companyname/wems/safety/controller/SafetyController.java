package com.companyname.wems.safety.controller;

import com.companyname.wems.safety.model.SafetyIncident;
import com.companyname.wems.safety.service.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/safety")
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService safetyService;

    // Record incident
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncident> recordIncident(@RequestBody SafetyIncident incident) {
        return ResponseEntity.ok(safetyService.recordIncident(incident));
    }

    // Update incident
    @PutMapping("/incidents/{id}")
    public ResponseEntity<SafetyIncident> updateIncident(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(safetyService.updateIncidentStatus(id, status, notes));
    }

    // List incidents
    @GetMapping("/incidents")
    public ResponseEntity<List<SafetyIncident>> listIncidents() {
        return ResponseEntity.ok(safetyService.listIncidents());
    }

    // Export OSHA report
    @GetMapping("/reports/osha")
    public ResponseEntity<List<SafetyIncident>> exportOSHAReport(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return ResponseEntity.ok(safetyService.exportOSHAReport(start, end));
    }
}