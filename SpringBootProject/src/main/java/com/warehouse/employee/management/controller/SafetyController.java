package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.SafetyIncidentDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/safety/incidents")
@Validated
public class SafetyController {
    private final List<SafetyIncidentDto> incidents = new ArrayList<>();

    @PreAuthorize("hasAuthority('SAFETY_REPORT')")
    @PostMapping
    public SafetyIncidentDto reportIncident(@Valid @RequestBody SafetyIncidentDto incident) {
        incident.setStatus("REPORTED");
        incidents.add(incident);
        return incident;
    }

    @PreAuthorize("hasAuthority('SAFETY_UPDATE')")
    @PutMapping("/{index}")
    public SafetyIncidentDto updateIncidentStatus(@PathVariable int index, @RequestParam String status) {
        if (index < 0 || index >= incidents.size()) throw new IllegalArgumentException("Invalid index");
        SafetyIncidentDto incident = incidents.get(index);
        incident.setStatus(status);
        return incident;
    }

    @PreAuthorize("hasAuthority('SAFETY_READ')")
    @GetMapping
    public List<SafetyIncidentDto> getIncidents() {
        return Collections.unmodifiableList(incidents);
    }
}
