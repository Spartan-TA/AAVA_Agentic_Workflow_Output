package com.warehouse.employee.controller;

import com.warehouse.employee.dto.SafetyIncidentDto;
import com.warehouse.employee.service.SafetyIncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for safety incidents.
 */
@RestController
@RequestMapping("/api/safety-incidents")
@Validated
public class SafetyIncidentController {

    private final SafetyIncidentService safetyIncidentService;

    @Autowired
    public SafetyIncidentController(SafetyIncidentService safetyIncidentService) {
        this.safetyIncidentService = safetyIncidentService;
    }

    @Operation(summary = "Report a safety incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incident reported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> reportIncident(@Valid @RequestBody SafetyIncidentDto dto) {
        SafetyIncidentDto response = safetyIncidentService.reportIncident(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Export all safety incidents in OSHA CSV format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV export successful")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/export/osha")
    public ResponseEntity<String> exportOSHA() {
        String csv = safetyIncidentService.exportOSHA();
        return ResponseEntity.ok(csv);
    }
}
