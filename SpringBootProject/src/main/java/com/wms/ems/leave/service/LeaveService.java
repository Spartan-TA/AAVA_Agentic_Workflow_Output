package com.wms.ems.leave.service;

import com.wms.ems.leave.repository.LeaveRequestRepository;
import com.wms.ems.leave.dto.LeaveRequestDto;
import com.wms.ems.leave.entity.LeaveRequest;
import com.wms.ems.leave.entity.LeaveStatus;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Leave management business logic and operations.
 */
@Slf4j
@Service
@Transactional
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Requests leave for an employee after validation.
     * @param dto LeaveRequestDto
     * @return LeaveRequest
     */
    public LeaveRequest requestLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));
        LeaveRequest request = new LeaveRequest(dto);
        request.setEmployee(employee);
        request.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(request);
    }

    /**
     * Approves a leave request.
     * @param requestId LeaveRequest ID
     * @param approver Approver name
     * @return LeaveRequest
     */
    public LeaveRequest approveLeave(Long requestId, String approver) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + requestId));
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ValidationException("Leave request is not pending");
        }
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprover(approver);
        return leaveRequestRepository.save(request);
    }

    /**
     * Denies a leave request.
     * @param requestId LeaveRequest ID
     * @param approver Approver name
     * @param reason Reason for denial
     * @return LeaveRequest
     */
    public LeaveRequest denyLeave(Long requestId, String approver, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + requestId));
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ValidationException("Leave request is not pending");
        }
        request.setStatus(LeaveStatus.DENIED);
        request.setApprover(approver);
        request.setDenialReason(reason);
        return leaveRequestRepository.save(request);
    }

    /**
     * Gets leave requests for an employee filtered by status.
     * @param employeeId Employee ID
     * @param status LeaveStatus
     * @return List<LeaveRequest>
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId, LeaveStatus status) {
        return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
    }

    /**
     * Gets all pending leave requests.
     * @return List<LeaveRequest>
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }
}
