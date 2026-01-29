package com.company.wms.safety.controller;

import com.company.wms.safety.model.SafetyIncident;
import com.company.wms.safety.service.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

/**
 * REST controller for safety incident management.
 */
@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService safetyService;

    @GetMapping
    public List<SafetyIncident> getAllIncidents() {
        return safetyService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SafetyIncident> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(safetyService.getIncidentById(id));
    }

    @GetMapping("/reporter/{employeeId}")
    public List<SafetyIncident> getIncidentsByReporter(@PathVariable Long employeeId) {
        return safetyService.getIncidentsByReporter(employeeId);
    }

    @PostMapping
    public ResponseEntity<SafetyIncident> createIncident(@RequestBody SafetyIncident incident) {
        SafetyIncident created = safetyService.createIncident(incident);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SafetyIncident> updateIncident(@PathVariable Long id, @RequestBody SafetyIncident incident) {
        SafetyIncident updated = safetyService.updateIncident(id, incident);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        safetyService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}
