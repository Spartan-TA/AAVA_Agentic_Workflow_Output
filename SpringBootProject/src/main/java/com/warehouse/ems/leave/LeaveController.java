package com.warehouse.ems.leave;

import com.warehouse.ems.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing leave requests.
 */
@RestController
@RequestMapping("/api/leave/requests")
@Validated
public class LeaveController {

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveService.getAllLeaveRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveRequestById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsByEmployee(employeeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsByStatus(status));
    }

    @GetMapping("/overlap")
    public ResponseEntity<List<LeaveRequest>> getOverlappingLeaves(@RequestParam Long employeeId, @RequestParam String startDate, @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return ResponseEntity.ok(leaveService.getOverlappingLeaves(employeeId, start, end));
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest created = leaveService.createLeaveRequest(leaveRequest);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(@PathVariable Long id, @Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest updated = leaveService.updateLeaveRequest(id, leaveRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteLeaveRequest(@PathVariable Long id) {
        leaveService.deleteLeaveRequest(id);
        return ResponseEntity.ok(new ApiResponse(true, "Leave request deleted successfully"));
    }
}
