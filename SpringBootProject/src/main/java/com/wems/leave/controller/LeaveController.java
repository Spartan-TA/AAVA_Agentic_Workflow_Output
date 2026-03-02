package com.wems.leave.controller;

import com.wems.leave.domain.LeaveRequest;
import com.wems.leave.domain.LeaveType;
import com.wems.leave.domain.LeaveStatus;
import com.wems.leave.domain.LeaveBalance;
import com.wems.leave.service.LeaveService;
import com.wems.employee.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @PostMapping("/requests")
    public LeaveRequest requestLeave(@RequestParam Long employeeId, @RequestParam LeaveType type, @RequestParam BigDecimal days, @RequestParam String reason, @RequestParam String start, @RequestParam String end) {
        Employee employee = null; // TODO: resolve employee
        return leaveService.requestLeave(employee, type, days, reason, LocalDateTime.parse(start), LocalDateTime.parse(end));
    }

    @PostMapping("/{id}/approve")
    public LeaveRequest approveLeave(@PathVariable Long id, @RequestParam Long approverId, @RequestParam String notes) {
        Employee approver = null; // TODO: resolve approver
        return leaveService.approveLeave(id, approver, notes);
    }

    @PostMapping("/{id}/deny")
    public LeaveRequest denyLeave(@PathVariable Long id, @RequestParam Long approverId, @RequestParam String notes) {
        Employee approver = null; // TODO: resolve approver
        return leaveService.denyLeave(id, approver, notes);
    }

    @GetMapping("/balance")
    public LeaveBalance getBalance(@RequestParam Long employeeId, @RequestParam LeaveType type) {
        Employee employee = null; // TODO: resolve employee
        return leaveService.getBalance(employee, type);
    }
}
