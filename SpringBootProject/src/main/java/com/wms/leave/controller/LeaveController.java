package com.wms.leave.controller;

import com.wms.leave.dto.LeaveRequestDto;
import com.wms.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Leave management endpoints.
 */
@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave", description = "Leave Management API")
@Validated
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Operation(summary = "Submit a leave request")
    @PostMapping
    public ResponseEntity<LeaveRequestDto> submitLeaveRequest(@Valid @RequestBody LeaveRequestDto leaveRequestDto) {
        return ResponseEntity.ok(leaveService.submitLeaveRequest(leaveRequestDto));
    }

    @Operation(summary = "Get leave requests by employee ID")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveRequestsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsByEmployeeId(employeeId));
    }

    @Operation(summary = "Get all leave requests")
    @GetMapping
    public ResponseEntity<List<LeaveRequestDto>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveService.getAllLeaveRequests());
    }

    @Operation(summary = "Approve a leave request")
    @PutMapping("/approve/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> approveLeave(@PathVariable Long leaveRequestId) {
        return ResponseEntity.ok(leaveService.approveLeave(leaveRequestId));
    }

    @Operation(summary = "Reject a leave request")
    @PutMapping("/reject/{leaveRequestId}")
    public ResponseEntity<LeaveRequestDto> rejectLeave(@PathVariable Long leaveRequestId) {
        return ResponseEntity.ok(leaveService.rejectLeave(leaveRequestId));
    }
}
