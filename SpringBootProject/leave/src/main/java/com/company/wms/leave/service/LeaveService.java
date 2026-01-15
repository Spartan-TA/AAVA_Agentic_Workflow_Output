package com.company.wms.leave.service;

import com.company.wms.leave.domain.LeaveRequest;
import com.company.wms.leave.domain.LeaveRequest.LeaveStatus;
import com.company.wms.leave.repository.LeaveRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing leave requests.
 * Handles leave request creation, approval/denial workflow, and balance calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveService {

    private final LeaveRepository leaveRepository;

    /**
     * Create a new leave request.
     * @param leaveRequest the leave request to create
     * @return the created leave request
     */
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        log.info("Creating leave request for employee: {}", leaveRequest.getEmployeeId());
        
        // Validate dates
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlapping = leaveRepository.findOverlappingLeaveRequests(
            leaveRequest.getEmployeeId(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            LeaveStatus.APPROVED
        );
        
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Employee already has approved leave during this period");
        }
        
        leaveRequest.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = leaveRepository.save(leaveRequest);
        log.info("Leave request created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Approve a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param approverId the ID of the approver
     * @param comments optional approver comments
     * @return the approved leave request
     */
    public LeaveRequest approveLeaveRequest(Long leaveRequestId, Long approverId, String comments) {
        log.info("Approving leave request: {} by approver: {}", leaveRequestId, approverId);
        
        LeaveRequest leaveRequest = leaveRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));
        
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be approved");
        }
        
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setApproverComments(comments);
        
        LeaveRequest approved = leaveRepository.save(leaveRequest);
        log.info("Leave request approved: {}", leaveRequestId);
        
        // TODO: Send notification to employee
        // TODO: Update leave balance
        // TODO: Flag scheduled shifts for coverage
        
        return approved;
    }

    /**
     * Deny a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param approverId the ID of the approver
     * @param comments required denial reason
     * @return the denied leave request
     */
    public LeaveRequest denyLeaveRequest(Long leaveRequestId, Long approverId, String comments) {
        log.info("Denying leave request: {} by approver: {}", leaveRequestId, approverId);
        
        if (comments == null || comments.trim().isEmpty()) {
            throw new IllegalArgumentException("Denial reason is required");
        }
        
        LeaveRequest leaveRequest = leaveRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));
        
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be denied");
        }
        
        leaveRequest.setStatus(LeaveStatus.DENIED);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setApproverComments(comments);
        
        LeaveRequest denied = leaveRepository.save(leaveRequest);
        log.info("Leave request denied: {}", leaveRequestId);
        
        // TODO: Send notification to employee
        
        return denied;
    }

    /**
     * Get all leave requests for an employee.
     * @param employeeId the employee ID
     * @param pageable pagination information
     * @return page of leave requests
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching leave requests for employee: {}", employeeId);
        return leaveRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    /**
     * Get all pending leave requests for approval.
     * @param pageable pagination information
     * @return page of pending leave requests
     */
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getPendingLeaveRequests(Pageable pageable) {
        log.debug("Fetching pending leave requests");
        return leaveRepository.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING, pageable);
    }

    /**
     * Cancel a leave request.
     * @param leaveRequestId the ID of the leave request
     * @param employeeId the ID of the employee (for authorization)
     * @return the cancelled leave request
     */
    public LeaveRequest cancelLeaveRequest(Long leaveRequestId, Long employeeId) {
        log.info("Cancelling leave request: {} by employee: {}", leaveRequestId, employeeId);
        
        LeaveRequest leaveRequest = leaveRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));
        
        if (!leaveRequest.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Employee can only cancel their own leave requests");
        }
        
        if (leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new IllegalStateException("Leave request is already cancelled");
        }
        
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest cancelled = leaveRepository.save(leaveRequest);
        log.info("Leave request cancelled: {}", leaveRequestId);
        
        return cancelled;
    }

    /**
     * Calculate remaining leave balance for an employee.
     * @param employeeId the employee ID
     * @param leaveType the leave type
     * @return remaining balance in days
     */
    @Transactional(readOnly = true)
    public double calculateLeaveBalance(Long employeeId, LeaveRequest.LeaveType leaveType) {
        log.debug("Calculating leave balance for employee: {} and type: {}", employeeId, leaveType);
        
        // TODO: Get employee's annual leave allocation from employee service
        double annualAllocation = 15.0; // Default PTO days
        
        // Calculate used leave days
        List<LeaveRequest> approvedLeaves = leaveRepository.findByEmployeeIdAndLeaveTypeAndStatus(
            employeeId, leaveType, LeaveStatus.APPROVED
        );
        
        double usedDays = approvedLeaves.stream()
            .mapToLong(LeaveRequest::getDaysCount)
            .sum();
        
        double remaining = annualAllocation - usedDays;
        log.debug("Leave balance for employee {}: {} days remaining", employeeId, remaining);
        
        return Math.max(0, remaining);
    }
}