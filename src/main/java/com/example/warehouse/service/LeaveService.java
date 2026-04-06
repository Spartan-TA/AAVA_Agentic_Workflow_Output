package com.example.warehouse.service;

import com.example.warehouse.dto.LeaveRequestDTO;
import com.example.warehouse.dto.LeaveApprovalDTO;
import com.example.warehouse.entity.LeaveRequest;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.LeaveRequestRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        // Check accrual balance
        if (employee.getLeaveBalance() < dto.getDays()) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setDays(dto.getDays());
        request.setType(dto.getType());
        request.setStatus("PENDING");
        leaveRequestRepository.save(request);
        // Scheduling integration, payroll hooks can be triggered here
        return request;
    }

    @Transactional
    public LeaveRequest approveLeave(Long requestId, LeaveApprovalDTO dto) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        request.setStatus(dto.isApproved() ? "APPROVED" : "REJECTED");
        if (dto.isApproved()) {
            Employee employee = request.getEmployee();
            employee.setLeaveBalance(employee.getLeaveBalance() - request.getDays());
            employeeRepository.save(employee);
        }
        leaveRequestRepository.save(request);
        // Payroll hooks
        return request;
    }

    public List<LeaveRequest> getLeaveBalance(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }
}
