package com.company.wms.scheduling.controller;

import com.company.wms.scheduling.domain.ShiftTemplate;
import com.company.wms.scheduling.domain.ShiftAssignment;
import com.company.wms.scheduling.service.SchedulingService;
import com.company.wms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for shift templates and assignments.
 */
@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    private final SchedulingService schedulingService;

    @Autowired
    public ShiftController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    /**
     * Create a shift template.
     */
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<ShiftTemplate>> createTemplate(@Valid @RequestBody ShiftTemplate template) {
        ShiftTemplate saved = schedulingService.createTemplate(template);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    /**
     * List all shift templates.
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ShiftTemplate>>> listTemplates() {
        List<ShiftTemplate> templates = schedulingService.listTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    /**
     * Get shift template by ID.
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<ShiftTemplate>> getTemplate(@PathVariable Long id) {
        ShiftTemplate template = schedulingService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    /**
     * Assign a shift to an employee.
     */
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<ShiftAssignment>> assignShift(@Valid @RequestBody ShiftAssignment assignment) {
        ShiftAssignment saved = schedulingService.assignShift(assignment);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    /**
     * Get shift assignments for employee.
     */
    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ShiftAssignment>>> getAssignmentsForEmployee(@PathVariable Long employeeId) {
        List<ShiftAssignment> assignments = schedulingService.getAssignmentsForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    /**
     * Get shift assignments for employee in date range.
     */
    @GetMapping("/assignments/employee/{employeeId}/range")
    public ResponseEntity<ApiResponse<List<ShiftAssignment>>> getAssignmentsForEmployeeInRange(
            @PathVariable Long employeeId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        List<ShiftAssignment> assignments = schedulingService.getAssignmentsForEmployeeInRange(employeeId, start, end);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
}
