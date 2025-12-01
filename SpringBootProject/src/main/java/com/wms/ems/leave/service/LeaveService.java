package com.wms.ems.leave.service;

import com.wms.ems.leave.entity.LeaveRequest;
import com.wms.ems.leave.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for Leave management.
 * Handles request, approve/deny, and balance calculation.
 */
@Service
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;

    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * Submit a new leave request.
     * @param request the leave request
     * @return the saved LeaveRequest
     */
    public LeaveRequest submitRequest(LeaveRequest request) {
        request.setStatus("PENDING");
        // ... set other fields as needed
        return leaveRequestRepository.save(request);
    }

    /**
     * Approve a leave request.
     * @param requestId the request ID
     * @return the updated LeaveRequest
     */
    public LeaveRequest approveRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        request.setStatus("APPROVED");
        return leaveRequestRepository.save(request);
    }

    /**
     * Deny a leave request.
     * @param requestId the request ID
     * @param reason the denial reason
     * @return the updated LeaveRequest
     */
    public LeaveRequest denyRequest(Long requestId, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        request.setStatus("DENIED");
        request.setDenialReason(reason);
        return leaveRequestRepository.save(request);
    }

    /**
     * Get leave balance for an employee (stub).
     * @param employeeId the employee's ID
     * @return leave balance (int)
     */
    public int getLeaveBalance(Long employeeId) {
        // Implement leave balance calculation logic here
        return 20; // Example: default 20 days
    }

    /**
     * Get all leave requests for an employee.
     * @param employeeId the employee's ID
     * @return List of LeaveRequest
     */
    public List<LeaveRequest> getRequestsForEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }
}
