package com.warehouse.employee.controller;

import com.warehouse.employee.dto.ShiftAssignmentRequest;
import com.warehouse.employee.dto.ShiftAssignmentResponse;
import com.warehouse.employee.service.ShiftService;
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
import java.util.List;

/**
 * REST controller for shift templates and assignments.
 */
@RestController
@RequestMapping("/api/shifts")
@Validated
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @Operation(summary = "Assign a shift to an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shift assigned successfully"),
            @ApiResponse(responseCode = "409", description = "Shift conflict")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/assign")
    public ResponseEntity<ShiftAssignmentResponse> assignShift(@Valid @RequestBody ShiftAssignmentRequest request) {
        ShiftAssignmentResponse response = shiftService.assignShift(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all shifts assigned to an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of shift assignments")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentResponse>> getEmployeeShifts(@PathVariable Long employeeId) {
        List<ShiftAssignmentResponse> shifts = shiftService.getEmployeeShifts(employeeId);
        return ResponseEntity.ok(shifts);
    }
}
