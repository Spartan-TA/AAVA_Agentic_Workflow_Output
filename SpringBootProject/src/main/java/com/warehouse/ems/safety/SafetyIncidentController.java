package com.warehouse.ems.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for SafetyIncident endpoints.
 */
@RestController
@RequestMapping("/safety/incidents")
@Validated
public class SafetyIncidentController {
    private final SafetyIncidentService incidentService;

    @Autowired
    public SafetyIncidentController(SafetyIncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**
     * Record a new safety incident.
     */
    @PostMapping
    public ResponseEntity<SafetyIncident> recordIncident(@Valid @RequestBody SafetyIncident incident) {
        try {
            SafetyIncident created = incidentService.recordIncident(incident);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all safety incidents.
     */
    @GetMapping
    public ResponseEntity<List<SafetyIncident>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    /**
     * Update the status of an incident.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<SafetyIncident> updateStatus(@PathVariable Long id, @RequestParam SafetyIncident.Status status) {
        try {
            SafetyIncident updated = incidentService.updateIncidentStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Generate OSHA 300/300A report for a date range.
     */
    @GetMapping("/osha-report")
    public ResponseEntity<List<SafetyIncident>> getOshaReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(incidentService.generateOshaReport(start, end));
    }
}
