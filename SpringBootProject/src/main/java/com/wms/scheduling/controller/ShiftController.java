package com.wms.scheduling.controller;

import com.wms.scheduling.dto.ShiftTemplateDto;
import com.wms.scheduling.dto.ShiftAssignmentDto;
import com.wms.scheduling.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Shift scheduling endpoints.
 */
@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shift", description = "Shift Scheduling API")
@Validated
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @Operation(summary = "Create a new shift template")
    @PostMapping("/template")
    public ResponseEntity<ShiftTemplateDto> createShiftTemplate(@Valid @RequestBody ShiftTemplateDto shiftTemplateDto) {
        return ResponseEntity.ok(shiftService.createShiftTemplate(shiftTemplateDto));
    }

    @Operation(summary = "Get all shift templates")
    @GetMapping("/template")
    public ResponseEntity<List<ShiftTemplateDto>> getAllShiftTemplates() {
        return ResponseEntity.ok(shiftService.getAllShiftTemplates());
    }

    @Operation(summary = "Assign a shift to an employee")
    @PostMapping("/assignment")
    public ResponseEntity<ShiftAssignmentDto> assignShift(@Valid @RequestBody ShiftAssignmentDto shiftAssignmentDto) {
        return ResponseEntity.ok(shiftService.assignShift(shiftAssignmentDto));
    }

    @Operation(summary = "Get shift assignments by employee ID")
    @GetMapping("/assignment/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentDto>> getAssignmentsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftService.getAssignmentsByEmployeeId(employeeId));
    }

    @Operation(summary = "Get shift assignments by date")
    @GetMapping("/assignment/date/{assignmentDate}")
    public ResponseEntity<List<ShiftAssignmentDto>> getAssignmentsByDate(@PathVariable String assignmentDate) {
        LocalDate date = LocalDate.parse(assignmentDate);
        return ResponseEntity.ok(shiftService.getAssignmentsByDate(date));
    }
}
