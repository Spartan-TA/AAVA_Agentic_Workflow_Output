package com.example.warehouse.safety.controller;

import com.example.warehouse.safety.entity.SafetyIncident;
import com.example.warehouse.safety.service.SafetyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    @Autowired
    private SafetyService safetyService;

    // Get all safety incidents
    @GetMapping
    public List<SafetyIncident> getAllIncidents() {
        return safetyService.getAllIncidents();
    }

    // Get safety incidents by employee
    @GetMapping("/employee/{employeeId}")
    public List<SafetyIncident> getIncidentsByEmployee(@PathVariable Long employeeId) {
        return safetyService.getIncidentsByEmployee(employeeId);
    }

    // Get safety incident by ID
    @GetMapping("/{id}")
    public ResponseEntity<SafetyIncident> getIncidentById(@PathVariable Long id) {
        Optional<SafetyIncident> incident = safetyService.getIncidentById(id);
        return incident.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new safety incident
    @PostMapping
    public ResponseEntity<SafetyIncident> createIncident(@RequestBody SafetyIncident incident) {
        SafetyIncident created = safetyService.createIncident(incident);
        return ResponseEntity.ok(created);
    }

    // Update safety incident
    @PutMapping("/{id}")
    public ResponseEntity<SafetyIncident> updateIncident(@PathVariable Long id, @RequestBody SafetyIncident incident) {
        Optional<SafetyIncident> updated = safetyService.updateIncident(id, incident);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete safety incident
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        boolean deleted = safetyService.deleteIncident(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
