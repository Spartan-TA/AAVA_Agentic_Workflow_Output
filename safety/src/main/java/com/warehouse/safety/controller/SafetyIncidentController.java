package com.warehouse.safety.controller;

import com.warehouse.safety.entity.SafetyIncident;
import com.warehouse.safety.service.SafetyIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/safety/incidents")
public class SafetyIncidentController {
    @Autowired
    private SafetyIncidentService safetyIncidentService;

    @GetMapping
    public ResponseEntity<List<SafetyIncident>> getAllIncidents() {
        return ResponseEntity.ok(safetyIncidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SafetyIncident> getIncidentById(@PathVariable Long id) {
        return safetyIncidentService.getIncidentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<SafetyIncident>> getIncidentsBySeverity(@PathVariable SafetyIncident.Severity severity) {
        return ResponseEntity.ok(safetyIncidentService.getIncidentsBySeverity(severity));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SafetyIncident>> getIncidentsByStatus(@PathVariable SafetyIncident.Status status) {
        return ResponseEntity.ok(safetyIncidentService.getIncidentsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<SafetyIncident> reportIncident(@Valid @RequestBody SafetyIncident incident) {
        SafetyIncident created = safetyIncidentService.reportIncident(incident);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/investigate")
    public ResponseEntity<SafetyIncident> startInvestigation(@PathVariable Long id) {
        SafetyIncident updated = safetyIncidentService.startInvestigation(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<SafetyIncident> resolveIncident(@PathVariable Long id) {
        SafetyIncident updated = safetyIncidentService.resolveIncident(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<SafetyIncident> closeIncident(@PathVariable Long id) {
        SafetyIncident updated = safetyIncidentService.closeIncident(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/osha-export")
    public ResponseEntity<String> exportToOSHA(@PathVariable Long id) {
        String result = safetyIncidentService.exportToOSHA(id);
        return ResponseEntity.ok(result);
    }
}
