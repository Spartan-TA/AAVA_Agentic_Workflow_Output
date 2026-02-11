package com.warehouse.employee.controller;

import com.warehouse.employee.dto.LeaveRequestDto;
import com.warehouse.employee.service.LeaveService;
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
 * REST controller for leave requests.
 */
@RestController
@RequestMapping("/api/leave")
@Validated
public class LeaveController {

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Operation(summary = "Request leave for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leave requested successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> requestLeave(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequestDto response = leaveService.requestLeave(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Approve leave request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave approved successfully"),
            @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/approve/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> approveLeave(@PathVariable Long leaveRequestId) {
        LeaveRequestDto response = leaveService.approveLeave(leaveRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get leave balance for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave balance calculated")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','MANAGER')")
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<Integer> getLeaveBalance(@PathVariable Long employeeId) {
        int balance = leaveService.getLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
}
