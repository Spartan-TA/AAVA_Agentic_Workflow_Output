package com.warehouse.ems.shift.controller;

import com.warehouse.ems.shift.entity.ShiftAssignment;
import com.warehouse.ems.shift.entity.ShiftTemplate;
import com.warehouse.ems.shift.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for shift management operations.
 */
@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shift", description = "Shift management endpoints")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @Operation(summary = "Create a new shift template")
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplate> createShiftTemplate(@Valid @RequestBody ShiftTemplate template) {
        return ResponseEntity.ok(shiftService.createShiftTemplate(template));
    }

    @Operation(summary = "Get all shift templates")
    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplate>> getAllShiftTemplates() {
        return ResponseEntity.ok(shiftService.getAllShiftTemplates());
    }

    @Operation(summary = "Assign shift to employee")
    @PostMapping("/assign")
    public ResponseEntity<ShiftAssignment> assignShift(@RequestParam Long employeeId,
                                                      @RequestParam Long templateId,
                                                      @RequestParam String date) {
        LocalDate shiftDate = LocalDate.parse(date);
        return ResponseEntity.ok(shiftService.assignShift(employeeId, templateId, shiftDate));
    }

    @Operation(summary = "Get all shift assignments for employee")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignment>> getAssignmentsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftService.getAssignmentsForEmployee(employeeId));
    }

    @Operation(summary = "Check shift conflict for employee")
    @GetMapping("/conflict")
    public ResponseEntity<Boolean> hasShiftConflict(@RequestParam Long employeeId,
                                                   @RequestParam String date,
                                                   @RequestParam Long templateId) {
        LocalDate shiftDate = LocalDate.parse(date);
        return ResponseEntity.ok(shiftService.hasShiftConflict(employeeId, shiftDate, templateId));
    }
}
