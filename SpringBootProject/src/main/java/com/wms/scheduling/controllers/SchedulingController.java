package com.wms.scheduling.controllers;

import com.wms.scheduling.dtos.ShiftAssignmentDto;
import com.wms.scheduling.dtos.ShiftTemplateDto;
import com.wms.scheduling.services.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for scheduling operations
 */
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {
    private final SchedulingService schedulingService;

    /**
     * Create a new shift template
     */
    @PostMapping("/shift-templates")
    public ResponseEntity<ShiftTemplateDto> createShiftTemplate(@RequestBody ShiftTemplateDto dto) {
        return ResponseEntity.ok(schedulingService.createShiftTemplate(dto));
    }

    /**
     * Get all shift templates
     */
    @GetMapping("/shift-templates")
    public ResponseEntity<List<ShiftTemplateDto>> getAllShiftTemplates() {
        return ResponseEntity.ok(schedulingService.getAllShiftTemplates());
    }

    /**
     * Assign a shift to an employee
     */
    @PostMapping("/assignments")
    public ResponseEntity<ShiftAssignmentDto> assignShift(@RequestBody ShiftAssignmentDto dto) {
        return ResponseEntity.ok(schedulingService.assignShift(dto));
    }

    /**
     * Get all assignments for an employee
     */
    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentDto>> getAssignmentsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(schedulingService.getAssignmentsForEmployee(employeeId));
    }

    /**
     * Get all assignments for a date
     */
    @GetMapping("/assignments/date/{date}")
    public ResponseEntity<List<ShiftAssignmentDto>> getAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(schedulingService.getAssignmentsForDate(date));
    }
}
