package com.wms.ems.safety;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/safety")
public class SafetyController {

    @Autowired
    private SafetyService safetyService;

    @PostMapping("/incidents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<SafetyIncident> reportIncident(@RequestBody SafetyIncident incident) {
        return ResponseEntity.ok(safetyService.reportIncident(incident));
    }

    @PutMapping("/incidents/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<SafetyIncident> updateIncidentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(safetyService.updateIncidentStatus(id, status));
    }

    @GetMapping("/incidents")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<SafetyIncident>> getIncidents() {
        return ResponseEntity.ok(safetyService.getIncidents());
    }

    @GetMapping("/reports/osha")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<String> generateOSHAReport(@RequestParam String type) {
        return ResponseEntity.ok(safetyService.generateOSHAReport(type));
    }
}
