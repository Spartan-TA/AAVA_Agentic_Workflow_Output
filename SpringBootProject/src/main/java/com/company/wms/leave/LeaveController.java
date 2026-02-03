package com.company.wms.leave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing leave requests and balances.
 */
@RestController
@RequestMapping("/api/leave")
@Validated
public class LeaveController {
    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequest>> getAllLeaveRequests() {
        logger.info("API: Get all leave requests");
        return ResponseEntity.ok(leaveService.getAllLeaveRequests());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        logger.info("API: Get leave request by id {}", id);
        Optional<LeaveRequest> leaveRequest = leaveService.getLeaveRequestById(id);
        return leaveRequest.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/requests")
    public ResponseEntity<LeaveRequest> createLeaveRequest(@Valid @RequestBody LeaveRequest leaveRequest) {
        logger.info("API: Create leave request");
        LeaveRequest created = leaveService.createLeaveRequest(leaveRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(@PathVariable Long id, @Valid @RequestBody LeaveRequest leaveRequest) {
        logger.info("API: Update leave request {}", id);
        LeaveRequest updated = leaveService.updateLeaveRequest(id, leaveRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        logger.info("API: Delete leave request {}", id);
        leaveService.deleteLeaveRequest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balances/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getLeaveBalancesByEmployee(@PathVariable Long employeeId) {
        logger.info("API: Get leave balances for employee {}", employeeId);
        return ResponseEntity.ok(leaveService.getLeaveBalancesByEmployee(employeeId));
    }

    @PutMapping("/balances")
    public ResponseEntity<LeaveBalance> updateLeaveBalance(@Valid @RequestBody LeaveBalance leaveBalance) {
        logger.info("API: Update leave balance");
        LeaveBalance updated = leaveService.updateLeaveBalance(leaveBalance);
        return ResponseEntity.ok(updated);
    }
}
