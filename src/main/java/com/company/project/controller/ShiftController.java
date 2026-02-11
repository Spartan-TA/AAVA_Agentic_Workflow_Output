package com.company.project.controller;

import com.company.project.dto.ShiftAssignmentRequest;
import com.company.project.dto.ShiftAssignmentResponse;
import com.company.project.service.ShiftService;
import com.company.project.mapper.ShiftMapper;
import com.company.project.exception.ShiftConflictException;
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
@RequestMapping("/shifts")
@Tag(name = "Shift Management", description = "Manage shift assignments and templates")
public class ShiftController {

    private final ShiftService shiftService;
    private final ShiftMapper shiftMapper;

    @Autowired
    public ShiftController(ShiftService shiftService, ShiftMapper shiftMapper) {
        this.shiftService = shiftService;
        this.shiftMapper = shiftMapper;
    }

    @Operation(summary = "Assign shift to employee", responses = {
            @ApiResponse(responseCode = "201", description = "Shift assigned successfully"),
            @ApiResponse(responseCode = "409", description = "Shift conflict detected")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @PostMapping("/assign")
    public ResponseEntity<ShiftAssignmentResponse> assignShift(@Valid @RequestBody ShiftAssignmentRequest request) {
        try {
            var assignment = shiftService.assignShift(request);
            return ResponseEntity.status(201).body(shiftMapper.toResponse(assignment));
        } catch (ShiftConflictException e) {
            throw e;
        }
    }

    @Operation(summary = "Get all shift assignments", responses = {
            @ApiResponse(responseCode = "200", description = "List of shift assignments")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping("/assignments")
    public ResponseEntity<List<ShiftAssignmentResponse>> getAllAssignments() {
        var assignments = shiftService.getAllAssignments();
        return ResponseEntity.ok(shiftMapper.toResponseList(assignments));
    }
}
