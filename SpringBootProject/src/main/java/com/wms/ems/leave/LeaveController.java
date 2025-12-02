package com.wms.ems.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<LeaveRequest> requestLeave(@RequestBody LeaveRequest request) {
        return ResponseEntity.ok(leaveService.requestLeave(request));
    }

    @GetMapping("/requests/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequests(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveRequests(employeeId));
    }

    @GetMapping("/balances/{employeeId}/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Double> getLeaveBalance(@PathVariable Long employeeId, @PathVariable String type) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId, type));
    }

    @PostMapping("/approve/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable Long requestId) {
        return ResponseEntity.ok(leaveService.approveLeave(requestId));
    }

    @PostMapping("/deny/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<LeaveRequest> denyLeave(@PathVariable Long requestId) {
        return ResponseEntity.ok(leaveService.denyLeave(requestId));
    }
}
