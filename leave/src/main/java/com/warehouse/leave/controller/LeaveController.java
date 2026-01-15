package com.warehouse.leave.controller;

import com.warehouse.leave.entity.LeaveRequest;
import com.warehouse.leave.service.LeaveService;
import com.warehouse.leave.dto.LeaveDTO;
import com.warehouse.leave.dto.CreateLeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getLeavesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveById(@PathVariable Long id) {
        return leaveService.getLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> requestLeave(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveRequest leave = leaveService.requestLeave(request);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable Long id) {
        LeaveRequest leave = leaveService.approveLeave(id);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/{id}/deny")
    public ResponseEntity<LeaveRequest> denyLeave(@PathVariable Long id) {
        LeaveRequest leave = leaveService.denyLeave(id);
        return ResponseEntity.ok(leave);
    }

    @GetMapping("/employee/{employeeId}/balance")
    public ResponseEntity<Double> getLeaveBalance(@PathVariable Long employeeId) {
        double balance = leaveService.calculateLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
}
