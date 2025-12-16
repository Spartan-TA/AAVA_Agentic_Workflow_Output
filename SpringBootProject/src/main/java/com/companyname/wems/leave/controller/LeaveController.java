package com.companyname.wems.leave.controller;

import com.companyname.wems.leave.model.LeaveRequest;
import com.companyname.wems.leave.model.LeaveBalance;
import com.companyname.wems.leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    // Submit leave request
    @PostMapping("/request")
    public ResponseEntity<LeaveRequest> submitLeave(@RequestBody LeaveRequest request) {
        return ResponseEntity.ok(leaveService.submitLeaveRequest(request));
    }

    // Approve leave request
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(leaveService.approveLeaveRequest(id, approverId));
    }

    // Deny leave request
    @PutMapping("/{id}/deny")
    public ResponseEntity<LeaveRequest> denyLeave(@PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(leaveService.denyLeaveRequest(id, approverId));
    }

    // Get employee leave requests
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<LeaveRequest>> getEmployeeLeaveRequests(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaveRequests(id));
    }

    // Get leave balance
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalances(employeeId));
    }
}