package com.wms.safety.controllers;

import com.wms.safety.dtos.OSHAReportDto;
import com.wms.safety.dtos.SafetyIncidentDto;
import com.wms.safety.enums.IncidentStatus;
import com.wms.safety.services.SafetyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for safety incidents and OSHA reporting
 */
@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyController {
    private final SafetyService safetyService;

    /**
     * Report a new safety incident
     */
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDto> reportIncident(@RequestBody SafetyIncidentDto dto) {
        return ResponseEntity.ok(safetyService.reportIncident(dto));
    }

    /**
     * Get all incidents by status
     */
    @GetMapping("/incidents/status/{status}")
    public ResponseEntity<List<SafetyIncidentDto>> getIncidentsByStatus(@PathVariable IncidentStatus status) {
        return ResponseEntity.ok(safetyService.getIncidentsByStatus(status));
    }

    /**
     * Generate OSHA report for an incident
     */
    @GetMapping("/incidents/{incidentId}/osha-report")
    public ResponseEntity<OSHAReportDto> generateOSHAReport(@PathVariable Long incidentId) {
        return ResponseEntity.ok(safetyService.generateOSHAReport(incidentId));
    }
}
