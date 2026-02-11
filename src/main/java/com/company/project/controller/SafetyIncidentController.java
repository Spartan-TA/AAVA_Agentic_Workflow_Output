package com.company.project.controller;

import com.company.project.dto.SafetyIncidentDto;
import com.company.project.service.SafetyIncidentService;
import com.company.project.mapper.SafetyIncidentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/safety/incidents")
@Tag(name = "Safety Incident Management", description = "Manage safety incidents and OSHA reporting")
public class SafetyIncidentController {

    private final SafetyIncidentService safetyIncidentService;
    private final SafetyIncidentMapper safetyIncidentMapper;

    @Autowired
    public SafetyIncidentController(SafetyIncidentService safetyIncidentService, SafetyIncidentMapper safetyIncidentMapper) {
        this.safetyIncidentService = safetyIncidentService;
        this.safetyIncidentMapper = safetyIncidentMapper;
    }

    @Operation(summary = "Report safety incident", responses = {
            @ApiResponse(responseCode = "201", description = "Incident reported successfully")
    })
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> reportIncident(@Valid @RequestBody SafetyIncidentDto request) {
        var incident = safetyIncidentService.reportIncident(request);
        return ResponseEntity.status(201).body(safetyIncidentMapper.toDto(incident));
    }

    @Operation(summary = "Get all safety incidents", responses = {
            @ApiResponse(responseCode = "200", description = "List of safety incidents")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<List<SafetyIncidentDto>> getAllIncidents() {
        var incidents = safetyIncidentService.getAllIncidents();
        return ResponseEntity.ok(safetyIncidentMapper.toDtoList(incidents));
    }
}
