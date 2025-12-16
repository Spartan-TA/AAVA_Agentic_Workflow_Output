package com.companyname.wems.leave.service;

import com.companyname.wems.leave.model.LeaveRequest;
import com.companyname.wems.leave.model.LeaveBalance;
import com.companyname.wems.leave.repository.LeaveRequestRepository;
import com.companyname.wems.leave.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    // Submit leave request
    public LeaveRequest submitLeaveRequest(LeaveRequest request) {
        request.setStatus("PENDING");
        return leaveRequestRepository.save(request);
    }

    // Approve leave request
    public LeaveRequest approveLeaveRequest(Long id, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
        request.setStatus("APPROVED");
        request.setApprovedBy(approverId);
        // Deduct leave balance
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(request.getEmployeeId(), request.getLeaveType());
        double days = request.getEndDate().toEpochDay() - request.getStartDate().toEpochDay() + 1;
        balance.setBalance(balance.getBalance() - days);
        leaveBalanceRepository.save(balance);
        return leaveRequestRepository.save(request);
    }

    // Deny leave request
    public LeaveRequest denyLeaveRequest(Long id, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
        request.setStatus("DENIED");
        request.setApprovedBy(approverId);
        return leaveRequestRepository.save(request);
    }

    // Get leave requests for employee
    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    // Get leave balance for employee
    public List<LeaveBalance> getLeaveBalances(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    // Accrual calculation (monthly)
    public void accrueLeaveMonthly(Long employeeId) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employeeId);
        for (LeaveBalance balance : balances) {
            balance.setBalance(balance.getBalance() + balance.getAccrualRate());
            leaveBalanceRepository.save(balance);
        }
    }
}