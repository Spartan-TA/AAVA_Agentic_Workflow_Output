package com.example.warehouse.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    @Autowired
    private SafetyService safetyService;

    @GetMapping("/incidents")
    public List<SafetyIncident> getAllIncidents() {
        return safetyService.getAllIncidents();
    }

    @GetMapping("/incidents/{id}")
    public ResponseEntity<SafetyIncident> getIncidentById(@PathVariable Long id) {
        return safetyService.getIncidentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/incidents/employee/{employeeId}")
    public List<SafetyIncident> getIncidentsByEmployee(@PathVariable Long employeeId) {
        return safetyService.getIncidentsByEmployee(employeeId);
    }

    @PostMapping("/incidents")
    public SafetyIncident createIncident(@RequestBody SafetyIncidentDto dto) {
        return safetyService.createIncident(dto);
    }

    @DeleteMapping("/incidents/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        safetyService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}
