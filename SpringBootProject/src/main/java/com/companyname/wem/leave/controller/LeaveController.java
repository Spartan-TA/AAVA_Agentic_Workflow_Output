package com.companyname.wem.leave.controller;

import com.companyname.wem.leave.domain.LeaveRequest;
import com.companyname.wem.leave.dto.LeaveRequestDTO;
import com.companyname.wem.leave.service.LeaveService;
import jakarta.validation.Valid;
lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService service;

    @PostMapping("/requests")
    public ResponseEntity<LeaveRequest> requestLeave(@Valid @RequestBody LeaveRequestDTO dto) {
        LeaveRequest request = service.requestLeave(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<Void> approveLeave(@PathVariable Long id, @RequestParam String approver) {
        service.approveLeave(id, approver);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getEmployeeLeaveRequests(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getEmployeeLeaveRequests(employeeId));
    }
}
