package com.example.warehouse.leave.controller;

import com.example.warehouse.leave.entity.LeaveRequest;
import com.example.warehouse.leave.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    // Get all leave requests
    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveService.getAllLeaveRequests();
    }

    // Get leave requests by employee
    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return leaveService.getLeaveRequestsByEmployee(employeeId);
    }

    // Get leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        Optional<LeaveRequest> request = leaveService.getLeaveRequestById(id);
        return request.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new leave request
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest request) {
        LeaveRequest created = leaveService.createLeaveRequest(request);
        return ResponseEntity.ok(created);
    }

    // Update leave request status
    @PutMapping("/{id}/status")
    public ResponseEntity<LeaveRequest> updateLeaveStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<LeaveRequest> updated = leaveService.updateLeaveStatus(id, status);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete leave request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        boolean deleted = leaveService.deleteLeaveRequest(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
