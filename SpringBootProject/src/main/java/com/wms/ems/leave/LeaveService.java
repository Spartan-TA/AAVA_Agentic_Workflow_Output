package com.wms.ems.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Request leave
    @Transactional
    public LeaveRequest requestLeave(LeaveRequest request) {
        request.setStatus("Pending");
        // Accrual calculation logic can be added here
        return leaveRequestRepository.save(request);
    }

    // Approve leave
    @Transactional
    public LeaveRequest approveLeave(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId).orElseThrow();
        request.setStatus("Approved");
        // Update accrual balance logic here
        return leaveRequestRepository.save(request);
    }

    // Deny leave
    @Transactional
    public LeaveRequest denyLeave(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId).orElseThrow();
        request.setStatus("Denied");
        return leaveRequestRepository.save(request);
    }

    // Get leave requests for employee
    public List<LeaveRequest> getLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    // Get leave balances
    public double getLeaveBalance(Long employeeId, String type) {
        // Accrual calculation logic here
        return leaveRequestRepository.findBalanceByEmployeeIdAndType(employeeId, type);
    }
}
