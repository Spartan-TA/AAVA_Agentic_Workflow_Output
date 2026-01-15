package com.company.wms.leave.controller;

import com.company.wms.leave.domain.LeaveRequest;
import com.company.wms.leave.service.LeaveService;
import com.company.wms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for leave management operations.
 * Provides endpoints for creating, approving, and managing leave requests.
 */
@RestController
@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Leave Management", description = "APIs for managing employee leave requests")
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * Create a new leave request.
     * @param leaveRequest the leave request to create
     * @return the created leave request
     */
    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Create leave request", description = "Submit a new leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeaveRequest(
            @Valid @RequestBody LeaveRequest leaveRequest) {
        log.info("Creating leave request for employee: {}", leaveRequest.getEmployeeId());
        LeaveRequest created = leaveService.createLeaveRequest(leaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Leave request created successfully"));
    }

    /**
     * Approve a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param approverId the ID of the approver
     * @param comments optional approver comments
     * @return the approved leave request
     */
    @PutMapping("/requests/{leaveRequestId}/approve")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Approve leave request", description = "Approve a pending leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeaveRequest(
            @PathVariable Long leaveRequestId,
            @RequestParam Long approverId,
            @RequestParam(required = false) String comments) {
        log.info("Approving leave request: {}", leaveRequestId);
        LeaveRequest approved = leaveService.approveLeaveRequest(leaveRequestId, approverId, comments);
        return ResponseEntity.ok(ApiResponse.success(approved, "Leave request approved"));
    }

    /**
     * Deny a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param approverId the ID of the approver
     * @param comments required denial reason
     * @return the denied leave request
     */
    @PutMapping("/requests/{leaveRequestId}/deny")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Deny leave request", description = "Deny a pending leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> denyLeaveRequest(
            @PathVariable Long leaveRequestId,
            @RequestParam Long approverId,
            @RequestParam String comments) {
        log.info("Denying leave request: {}", leaveRequestId);
        LeaveRequest denied = leaveService.denyLeaveRequest(leaveRequestId, approverId, comments);
        return ResponseEntity.ok(ApiResponse.success(denied, "Leave request denied"));
    }

    /**
     * Get all leave requests for an employee.
     * @param employeeId the employee ID
     * @param pageable pagination information
     * @return page of leave requests
     */
    @GetMapping("/requests/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Get employee leave requests", description = "Retrieve all leave requests for an employee")
    public ResponseEntity<ApiResponse<Page<LeaveRequest>>> getEmployeeLeaveRequests(
            @PathVariable Long employeeId,
            Pageable pageable) {
        log.debug("Fetching leave requests for employee: {}", employeeId);
        Page<LeaveRequest> requests = leaveService.getLeaveRequestsByEmployee(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(requests, "Leave requests retrieved successfully"));
    }

    /**
     * Get all pending leave requests.
     * @param pageable pagination information
     * @return page of pending leave requests
     */
    @GetMapping("/requests/pending")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Get pending leave requests", description = "Retrieve all pending leave requests for approval")
    public ResponseEntity<ApiResponse<Page<LeaveRequest>>> getPendingLeaveRequests(Pageable pageable) {
        log.debug("Fetching pending leave requests");
        Page<LeaveRequest> requests = leaveService.getPendingLeaveRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(requests, "Pending leave requests retrieved successfully"));
    }

    /**
     * Cancel a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param employeeId the ID of the employee
     * @return the cancelled leave request
     */
    @PutMapping("/requests/{leaveRequestId}/cancel")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Cancel leave request", description = "Cancel a leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> cancelLeaveRequest(
            @PathVariable Long leaveRequestId,
            @RequestParam Long employeeId) {
        log.info("Cancelling leave request: {}", leaveRequestId);
        LeaveRequest cancelled = leaveService.cancelLeaveRequest(leaveRequestId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Leave request cancelled"));
    }

    /**
     * Get leave balance for an employee.
     * @param employeeId the employee ID
     * @param leaveType the leave type
     * @return remaining leave balance
     */
    @GetMapping("/balance/{employeeId}")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Get leave balance", description = "Get remaining leave balance for an employee")
    public ResponseEntity<ApiResponse<Double>> getLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam LeaveRequest.LeaveType leaveType) {
        log.debug("Fetching leave balance for employee: {} and type: {}", employeeId, leaveType);
        double balance = leaveService.calculateLeaveBalance(employeeId, leaveType);
        return ResponseEntity.ok(ApiResponse.success(balance, "Leave balance retrieved successfully"));
    }
}