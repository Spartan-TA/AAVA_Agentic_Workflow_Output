package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.LeaveRequest;
import com.warehouse.employee.management.repository.LeaveRequestRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing LeaveRequest entities.
 */
@Service
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;

    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * Get all leave requests.
     * @return List of leave requests
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    /**
     * Get leave request by ID.
     * @param id LeaveRequest ID
     * @return LeaveRequest entity
     */
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest not found with id: " + id));
    }

    /**
     * Create a new leave request.
     * @param leaveRequest LeaveRequest entity
     * @return Created leave request
     */
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // TODO: Add business logic for leave balance validation
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * Approve a leave request.
     * @param id LeaveRequest ID
     * @return Approved leave request
     */
    @Transactional
    public LeaveRequest approveLeaveRequest(Long id) {
        LeaveRequest request = getLeaveRequestById(id);
        request.setStatus("APPROVED");
        return leaveRequestRepository.save(request);
    }

    /**
     * Reject a leave request.
     * @param id LeaveRequest ID
     * @return Rejected leave request
     */
    @Transactional
    public LeaveRequest rejectLeaveRequest(Long id) {
        LeaveRequest request = getLeaveRequestById(id);
        request.setStatus("REJECTED");
        return leaveRequestRepository.save(request);
    }

    /**
     * Delete a leave request by ID.
     * @param id LeaveRequest ID
     */
    @Transactional
    public void deleteLeaveRequest(Long id) {
        LeaveRequest request = getLeaveRequestById(id);
        leaveRequestRepository.delete(request);
    }
}
