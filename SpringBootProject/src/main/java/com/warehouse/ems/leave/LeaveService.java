package com.warehouse.ems.leave;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {
    @Autowired
    private LeaveRepository leaveRepository;

    @Transactional
    public LeaveRequest requestLeave(Long employeeId, String type, LocalDate startDate, LocalDate endDate) {
        // Business logic: Validate leave dates and balance
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be before start date.");
        }
        LeaveBalance balance = leaveRepository.findBalanceByEmployeeAndType(employeeId, type);
        long daysRequested = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (balance == null || balance.getBalance() < daysRequested) {
            throw new ValidationException("Insufficient leave balance.");
        }
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(new Employee(employeeId));
        leave.setType(type);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setStatus("REQUESTED");
        leave.setBalance(balance.getBalance());
        return leaveRepository.save(leave);
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveId, Long approverId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found."));
        if (!"REQUESTED".equals(leave.getStatus())) {
            throw new ValidationException("Leave request is not in REQUESTED status.");
        }
        leave.setStatus("APPROVED");
        leave.setApprover(new Employee(approverId));
        // Update leave balance
        LeaveBalance balance = leaveRepository.findBalanceByEmployeeAndType(leave.getEmployee().getId(), leave.getType());
        long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        balance.setBalance(balance.getBalance() - days);
        return leaveRepository.save(leave);
    }

    public LeaveBalance getLeaveBalance(Long employeeId, String type) {
        return leaveRepository.findBalanceByEmployeeAndType(employeeId, type);
    }

    public List<LeaveRequest> getLeaveRequests(Long employeeId) {
        return leaveRepository.findByEmployee(employeeId);
    }
}
