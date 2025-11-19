package com.example.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/safety-incidents")
public class SafetyIncidentController {
    @Autowired
    private SafetyIncidentService safetyIncidentService;

    @GetMapping
    public List<SafetyIncident> getAllIncidents() {
        return safetyIncidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Optional<SafetyIncident> getIncidentById(@PathVariable Long id) {
        return safetyIncidentService.getIncidentById(id);
    }

    @PostMapping
    public SafetyIncident createIncident(@RequestBody SafetyIncident incident) {
        return safetyIncidentService.saveIncident(incident);
    }

    @PutMapping("/{id}")
    public SafetyIncident updateIncident(@PathVariable Long id, @RequestBody SafetyIncident incident) {
        incident.setId(id);
        return safetyIncidentService.saveIncident(incident);
    }

    @DeleteMapping("/{id}")
    public void deleteIncident(@PathVariable Long id) {
        safetyIncidentService.deleteIncident(id);
    }
}