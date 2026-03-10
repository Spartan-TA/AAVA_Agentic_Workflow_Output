package com.example.warehouse.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public LeaveRequest createLeaveRequest(LeaveRequestDto dto) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(dto.getEmployeeId());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setType(dto.getType());
        request.setStatus("PENDING");
        request.setReason(dto.getReason());
        return leaveRequestRepository.save(request);
    }

    public void deleteLeaveRequest(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    public LeaveBalance getLeaveBalance(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    public LeaveBalance updateLeaveBalance(LeaveBalance balance) {
        return leaveBalanceRepository.save(balance);
    }

    @Transactional
    public LeaveRequest approveLeave(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId).orElseThrow();
        request.setStatus("APPROVED");
        leaveRequestRepository.save(request);
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeId(request.getEmployeeId());
        int days = (int) (request.getEndDate().toEpochDay() - request.getStartDate().toEpochDay() + 1);
        switch (request.getType()) {
            case "ANNUAL":
                balance.setAnnualLeave(balance.getAnnualLeave() - days);
                break;
            case "SICK":
                balance.setSickLeave(balance.getSickLeave() - days);
                break;
            case "CASUAL":
                balance.setCasualLeave(balance.getCasualLeave() - days);
                break;
        }
        leaveBalanceRepository.save(balance);
        return request;
    }

    @Transactional
    public LeaveRequest rejectLeave(Long requestId, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId).orElseThrow();
        request.setStatus("REJECTED");
        request.setReason(reason);
        return leaveRequestRepository.save(request);
    }
}
