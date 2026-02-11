package com.company.project.controller;

import com.company.project.dto.LeaveRequestDto;
import com.company.project.service.LeaveService;
import com.company.project.mapper.LeaveMapper;
import com.company.project.dto.LeaveRequestDto;
import com.company.project.exception.AttendanceException;
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
@RequestMapping("/leave")
@Tag(name = "Leave Management", description = "Manage leave requests and approvals")
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveMapper leaveMapper;

    @Autowired
    public LeaveController(LeaveService leaveService, LeaveMapper leaveMapper) {
        this.leaveService = leaveService;
        this.leaveMapper = leaveMapper;
    }

    @Operation(summary = "Request leave", responses = {
            @ApiResponse(responseCode = "201", description = "Leave requested successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid leave request")
    })
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> requestLeave(@Valid @RequestBody LeaveRequestDto request) {
        var leave = leaveService.requestLeave(request);
        return ResponseEntity.status(201).body(leaveMapper.toDto(leave));
    }

    @Operation(summary = "Approve leave", responses = {
            @ApiResponse(responseCode = "200", description = "Leave approved successfully")
    })
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'HR', 'ADMIN')")
    @PutMapping("/approve/{id}")
    public ResponseEntity<LeaveRequestDto> approveLeave(@PathVariable Long id) {
        var leave = leaveService.approveLeave(id);
        return ResponseEntity.ok(leaveMapper.toDto(leave));
    }

    @Operation(summary = "Get all leave requests", responses = {
            @ApiResponse(responseCode = "200", description = "List of leave requests")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<List<LeaveRequestDto>> getAllLeaveRequests() {
        var leaves = leaveService.getAllLeaveRequests();
        return ResponseEntity.ok(leaveMapper.toDtoList(leaves));
    }
}
