package com.wems.leave.service;

import com.wems.leave.domain.*;
import com.wems.employee.domain.Employee;
import com.wems.common.exception.ResourceNotFoundException;
import com.wems.common.exception.BusinessValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeaveService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    public LeaveRequest requestLeave(Employee employee, LeaveType type, BigDecimal days, String reason, LocalDateTime start, LocalDateTime end) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setType(type);
        request.setTotalDays(days);
        request.setReason(reason);
        request.setStartDate(start.toLocalDate());
        request.setEndDate(end.toLocalDate());
        request.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest approveLeave(Long requestId, Employee approver, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessValidationException("Leave request is not pending");
        }
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovalNotes(notes);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest denyLeave(Long requestId, Employee approver, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessValidationException("Leave request is not pending");
        }
        request.setStatus(LeaveStatus.DENIED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovalNotes(notes);
        return leaveRequestRepository.save(request);
    }

    public LeaveBalance getBalance(Employee employee, LeaveType type) {
        Optional<LeaveBalance> balance = leaveBalanceRepository.findAll().stream()
                .filter(b -> b.getEmployee().equals(employee) && b.getLeaveType() == type)
                .findFirst();
        return balance.orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
    }
}
