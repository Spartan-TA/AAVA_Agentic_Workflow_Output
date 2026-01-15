package com.warehouse.leave.service;

import com.warehouse.leave.entity.LeaveRequest;
import com.warehouse.leave.repository.LeaveRepository;
import com.warehouse.leave.dto.LeaveDTO;
import com.warehouse.leave.dto.CreateLeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {
    @Autowired
    private LeaveRepository leaveRepository;

    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public List<LeaveRequest> getLeavesByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    public Optional<LeaveRequest> getLeaveById(Long id) {
        return leaveRepository.findById(id);
    }

    @Transactional
    public LeaveRequest requestLeave(CreateLeaveRequest request) {
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        LeaveRequest leave = LeaveRequest.builder()
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(LeaveRequest.LeaveStatus.REQUESTED)
                .balance(days)
                .employeeId(request.getEmployeeId())
                .build();
        return leaveRepository.save(leave);
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        return leaveRepository.save(leave);
    }

    @Transactional
    public LeaveRequest denyLeave(Long leaveId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.DENIED);
        return leaveRepository.save(leave);
    }

    public double calculateLeaveBalance(Long employeeId) {
        List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(employeeId);
        return leaves.stream()
                .filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
                .mapToDouble(LeaveRequest::getBalance)
                .sum();
    }
}
