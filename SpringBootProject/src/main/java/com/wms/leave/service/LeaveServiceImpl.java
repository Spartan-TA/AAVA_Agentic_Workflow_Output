package com.wms.leave.service;

import com.wms.leave.dto.LeaveRequestDto;
import com.wms.leave.domain.LeaveRequest;
import com.wms.leave.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LeaveService interface.
 * Handles business logic for leave management.
 */
@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Override
    public LeaveRequestDto submitLeaveRequest(LeaveRequestDto leaveRequestDto) {
        LeaveRequest request = new LeaveRequest(
            leaveRequestDto.getEmployeeId(),
            leaveRequestDto.getLeaveType(),
            leaveRequestDto.getStartDate(),
            leaveRequestDto.getEndDate(),
            "PENDING"
        );
        LeaveRequest saved = leaveRepository.save(request);
        return new LeaveRequestDto(saved);
    }

    @Override
    public List<LeaveRequestDto> getLeaveRequestsByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
            .map(LeaveRequestDto::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRepository.findAll().stream()
            .map(LeaveRequestDto::new)
            .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDto approveLeave(Long leaveRequestId) {
        LeaveRequest request = leaveRepository.findById(leaveRequestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus("APPROVED");
        LeaveRequest updated = leaveRepository.save(request);
        return new LeaveRequestDto(updated);
    }

    @Override
    public LeaveRequestDto rejectLeave(Long leaveRequestId) {
        LeaveRequest request = leaveRepository.findById(leaveRequestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus("REJECTED");
        LeaveRequest updated = leaveRepository.save(request);
        return new LeaveRequestDto(updated);
    }
}
