package com.warehouse.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for LeaveRequest business logic.
 */
@Service
public class LeaveService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        return leaveRequestRepository.findByStatus(status);
    }

    public LeaveRequest saveLeaveRequest(LeaveRequest leaveRequest) {
        return leaveRequestRepository.save(leaveRequest);
    }

    public void deleteLeaveRequest(Long id) {
        leaveRequestRepository.deleteById(id);
    }
}
