package com.example.warehouse.service;

import com.example.warehouse.dto.LeaveRequestDTO;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.LeaveRequest;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing leave requests.
 */
@Service
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get all leave requests for an employee.
     * @param employeeId Employee ID
     * @return List of LeaveRequestDTO
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return leaveRequestRepository.findByEmployee(employee).stream()
                .map(LeaveRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Submit a leave request.
     * @param employeeId Employee ID
     * @param dto LeaveRequestDTO
     * @return LeaveRequestDTO
     */
    @Transactional
    public LeaveRequestDTO submitLeaveRequest(Long employeeId, LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ValidationException("Leave start and end dates are required");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new ValidationException("Leave end date cannot be before start date");
        }
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setType(dto.getType());
        leaveRequest.setStatus("PENDING");
        leaveRequestRepository.save(leaveRequest);
        return LeaveRequestDTO.fromEntity(leaveRequest);
    }

    /**
     * Approve a leave request.
     * @param leaveRequestId LeaveRequest ID
     * @return LeaveRequestDTO
     */
    @Transactional
    public LeaveRequestDTO approveLeaveRequest(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveRequestId));
        leaveRequest.setStatus("APPROVED");
        leaveRequestRepository.save(leaveRequest);
        return LeaveRequestDTO.fromEntity(leaveRequest);
    }

    /**
     * Reject a leave request.
     * @param leaveRequestId LeaveRequest ID
     * @return LeaveRequestDTO
     */
    @Transactional
    public LeaveRequestDTO rejectLeaveRequest(Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveRequestId));
        leaveRequest.setStatus("REJECTED");
        leaveRequestRepository.save(leaveRequest);
        return LeaveRequestDTO.fromEntity(leaveRequest);
    }

    /**
     * Get all leave requests.
     * @return List of LeaveRequestDTO
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(LeaveRequestDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
