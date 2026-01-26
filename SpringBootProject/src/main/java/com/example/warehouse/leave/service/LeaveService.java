package com.example.warehouse.leave.service;

import com.example.warehouse.leave.entity.LeaveRequest;
import com.example.warehouse.leave.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {
    @Autowired
    private LeaveRepository leaveRepository;

    // Get all leave requests
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRepository.findAll();
    }

    // Get leave requests by employee
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    // Get leave request by ID
    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRepository.findById(id);
    }

    // Create new leave request
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest request) {
        request.setStatus("PENDING"); // Default status
        return leaveRepository.save(request);
    }

    // Update leave request status
    @Transactional
    public Optional<LeaveRequest> updateLeaveStatus(Long id, String status) {
        return leaveRepository.findById(id).map(existing -> {
            existing.setStatus(status);
            return leaveRepository.save(existing);
        });
    }

    // Delete leave request
    @Transactional
    public boolean deleteLeaveRequest(Long id) {
        if (leaveRepository.existsById(id)) {
            leaveRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
