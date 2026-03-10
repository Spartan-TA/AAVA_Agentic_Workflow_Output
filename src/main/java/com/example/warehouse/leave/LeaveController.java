package com.example.warehouse.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @GetMapping("/requests")
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveService.getAllLeaveRequests();
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        return leaveService.getLeaveRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/requests/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return leaveService.getLeaveRequestsByEmployee(employeeId);
    }

    @PostMapping("/requests")
    public LeaveRequest createLeaveRequest(@RequestBody LeaveRequestDto dto) {
        return leaveService.createLeaveRequest(dto);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        leaveService.deleteLeaveRequest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance/{employeeId}")
    public LeaveBalance getLeaveBalance(@PathVariable Long employeeId) {
        return leaveService.getLeaveBalance(employeeId);
    }

    @PutMapping("/balance")
    public LeaveBalance updateLeaveBalance(@RequestBody LeaveBalance balance) {
        return leaveService.updateLeaveBalance(balance);
    }

    @PostMapping("/requests/{id}/approve")
    public LeaveRequest approveLeave(@PathVariable Long id) {
        return leaveService.approveLeave(id);
    }

    @PostMapping("/requests/{id}/reject")
    public LeaveRequest rejectLeave(@PathVariable Long id, @RequestParam String reason) {
        return leaveService.rejectLeave(id, reason);
    }
}
