package com.wms.leave.controllers;

import com.wms.leave.dtos.LeaveBalanceDto;
import com.wms.leave.dtos.LeaveRequestDto;
import com.wms.leave.enums.LeaveStatus;
import com.wms.leave.enums.LeaveType;
import com.wms.leave.services.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for leave management
 */
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    /**
     * Submit a new leave request
     */
    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestDto> submitLeaveRequest(@RequestBody LeaveRequestDto dto) {
        return ResponseEntity.ok(leaveService.submitLeaveRequest(dto));
    }

    /**
     * Approve or reject a leave request
     */
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<LeaveRequestDto> updateLeaveStatus(
            @PathVariable Long id,
            @RequestParam LeaveStatus status,
            @RequestParam Long approverId) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, status, approverId));
    }

    /**
     * Get all leave requests for an employee
     */
    @GetMapping("/requests/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveRequestsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsForEmployee(employeeId));
    }

    /**
     * Get leave balance for an employee and leave type
     */
    @GetMapping("/balances/employee/{employeeId}/type/{leaveType}")
    public ResponseEntity<LeaveBalanceDto> getLeaveBalance(
            @PathVariable Long employeeId,
            @PathVariable LeaveType leaveType) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId, leaveType));
    }
}
