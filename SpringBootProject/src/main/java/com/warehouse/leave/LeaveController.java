package com.warehouse.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for LeaveRequest operations.
 */
@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveService.getAllLeaveRequests();
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return leaveService.getLeaveRequestsByEmployee(employeeId);
    }

    @GetMapping("/status/{status}")
    public List<LeaveRequest> getLeaveRequestsByStatus(@PathVariable String status) {
        return leaveService.getLeaveRequestsByStatus(status);
    }

    @PostMapping
    public LeaveRequest saveLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        return leaveService.saveLeaveRequest(leaveRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteLeaveRequest(@PathVariable Long id) {
        leaveService.deleteLeaveRequest(id);
    }
}
