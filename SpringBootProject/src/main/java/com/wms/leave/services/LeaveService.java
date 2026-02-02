package com.wms.leave.services;

import com.wms.leave.dtos.LeaveBalanceDto;
import com.wms.leave.dtos.LeaveRequestDto;
import com.wms.leave.enums.LeaveStatus;
import com.wms.leave.enums.LeaveType;
import com.wms.leave.model.LeaveBalance;
import com.wms.leave.model.LeaveRequest;
import com.wms.leave.repositories.LeaveBalanceRepository;
import com.wms.leave.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing leave requests and balances
 */
@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    /**
     * Submit a new leave request
     */
    @Transactional
    public LeaveRequestDto submitLeaveRequest(LeaveRequestDto dto) {
        // Check leave balance
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveType(dto.getEmployeeId(), dto.getLeaveType());
        if (balanceOpt.isEmpty() || balanceOpt.get().getBalance() < (dto.getEndDate().toEpochDay() - dto.getStartDate().toEpochDay() + 1)) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
        LeaveRequest request = LeaveRequest.builder()
                .employeeId(dto.getEmployeeId())
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(LeaveStatus.PENDING)
                .reason(dto.getReason())
                .approverId(dto.getApproverId())
                .build();
        LeaveRequest saved = leaveRequestRepository.save(request);
        dto.setId(saved.getId());
        dto.setStatus(saved.getStatus());
        return dto;
    }

    /**
     * Approve or reject a leave request
     */
    @Transactional
    public LeaveRequestDto updateLeaveStatus(Long requestId, LeaveStatus status, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        request.setStatus(status);
        request.setApproverId(approverId);
        // If approved, deduct balance
        if (status == LeaveStatus.APPROVED) {
            Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveType(request.getEmployeeId(), request.getLeaveType());
            if (balanceOpt.isPresent()) {
                LeaveBalance balance = balanceOpt.get();
                long days = request.getEndDate().toEpochDay() - request.getStartDate().toEpochDay() + 1;
                balance.setBalance(balance.getBalance() - days);
                leaveBalanceRepository.save(balance);
            }
        }
        LeaveRequest saved = leaveRequestRepository.save(request);
        return LeaveRequestDto.builder()
                .id(saved.getId())
                .employeeId(saved.getEmployeeId())
                .leaveType(saved.getLeaveType())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .status(saved.getStatus())
                .reason(saved.getReason())
                .approverId(saved.getApproverId())
                .build();
    }

    /**
     * Get all leave requests for an employee
     */
    public List<LeaveRequestDto> getLeaveRequestsForEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(r -> LeaveRequestDto.builder()
                        .id(r.getId())
                        .employeeId(r.getEmployeeId())
                        .leaveType(r.getLeaveType())
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .status(r.getStatus())
                        .reason(r.getReason())
                        .approverId(r.getApproverId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get leave balance for an employee and leave type
     */
    public LeaveBalanceDto getLeaveBalance(Long employeeId, LeaveType leaveType) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, leaveType)
                .orElseThrow(() -> new IllegalArgumentException("Leave balance not found"));
        return LeaveBalanceDto.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployeeId())
                .leaveType(balance.getLeaveType())
                .balance(balance.getBalance())
                .build();
    }
}
