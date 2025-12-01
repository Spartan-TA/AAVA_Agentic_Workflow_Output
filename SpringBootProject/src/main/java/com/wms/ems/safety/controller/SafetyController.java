package com.wms.ems.safety.controller;

import com.wms.ems.safety.dto.SafetyIncidentDto;
import com.wms.ems.safety.service.SafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/safety")
@RequiredArgsConstructor
@Tag(name = "Safety", description = "Endpoints for safety incident management")
public class SafetyController {
    private final SafetyService safetyService;

    @Operation(summary = "Report a safety incident")
    @PostMapping("/incidents")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<?> reportIncident(@Valid @RequestBody SafetyIncidentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(safetyService.reportIncident(dto));
    }

    @Operation(summary = "Get all safety incidents")
    @GetMapping("/incidents")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<SafetyIncidentDto>> getIncidents(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(safetyService.getIncidents(status));
    }

    @Operation(summary = "Update safety incident status")
    @PatchMapping("/incidents/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        safetyService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get OSHA safety reports")
    @GetMapping("/reports/osha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOshaReports(@RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(safetyService.getOshaReports(fromDate, toDate));
    }
}
