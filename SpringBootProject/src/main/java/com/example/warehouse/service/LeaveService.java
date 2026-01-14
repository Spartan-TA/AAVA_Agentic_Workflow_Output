package com.example.warehouse.service;

import com.example.warehouse.entity.LeaveRequest;
import com.example.warehouse.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for LeaveRequest operations.
 */
@Service
public class LeaveService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public List<LeaveRequest> getLeaveRequestsForEmployee(Long employeeId, LocalDate from, LocalDate to) {
        return leaveRequestRepository.findByEmployeeAndDateRange(employeeId, from, to);
    }

    public List<LeaveRequest> getPendingApprovalRequests() {
        return leaveRequestRepository.findAllPendingApproval();
    }

    @Transactional
    public LeaveRequest requestLeave(LeaveRequest leaveRequest) {
        leaveRequest.setStatus("PENDING_APPROVAL");
        // Accrual calculation logic would go here
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId).orElseThrow();
        leaveRequest.setStatus("APPROVED");
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest denyLeave(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId).orElseThrow();
        leaveRequest.setStatus("DENIED");
        return leaveRequestRepository.save(leaveRequest);
    }
}
