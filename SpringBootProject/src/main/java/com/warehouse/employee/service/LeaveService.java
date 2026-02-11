package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.LeaveRequest;
import com.warehouse.employee.dto.LeaveRequestDto;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.LeaveMapper;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for leave request/approval workflow and leave balance calculation.
 */
@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository,
                        LeaveMapper leaveMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveMapper = leaveMapper;
    }

    /**
     * Request leave for an employee.
     * @param dto LeaveRequestDto
     * @return LeaveRequestDto
     */
    @Transactional
    public LeaveRequestDto requestLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + dto.getEmployeeId()));
        LeaveRequest leaveRequest = leaveMapper.toEntity(dto);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus("PENDING");
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toDto(saved);
    }

    /**
     * Approve leave request.
     * @param leaveRequestId LeaveRequest ID
     * @return LeaveRequestDto
     */
    @Transactional
    public LeaveRequestDto approveLeave(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveRequestId));
        leaveRequest.setStatus("APPROVED");
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toDto(updated);
    }

    /**
     * Get leave balance for an employee (approved leaves).
     * @param employeeId Employee ID
     * @return int leave balance
     */
    @Transactional(readOnly = true)
    public int getLeaveBalance(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeAndStatus(employee, "APPROVED");
        return approvedLeaves.size();
    }
}
