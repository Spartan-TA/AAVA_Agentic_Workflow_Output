package com.warehouse.employee.management.leave.service;

import com.warehouse.employee.management.dto.LeaveDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class LeaveService {
    private final Map<Long, List<LeaveDto>> leaveRequests = new HashMap<>();

    @Transactional
    public LeaveDto requestLeave(LeaveDto leaveDto) {
        leaveDto.setStatus("PENDING");
        leaveRequests.computeIfAbsent(leaveDto.getEmployeeId(), k -> new ArrayList<>()).add(leaveDto);
        return leaveDto;
    }

    @Transactional
    public LeaveDto approveLeave(Long employeeId, int leaveIndex, Long approverId) {
        List<LeaveDto> leaves = leaveRequests.getOrDefault(employeeId, new ArrayList<>());
        if (leaveIndex < 0 || leaveIndex >= leaves.size()) throw new IllegalArgumentException("Invalid leave index");
        LeaveDto leave = leaves.get(leaveIndex);
        leave.setStatus("APPROVED");
        leave.setApproverId(approverId);
        return leave;
    }

    public int calculateAccruedLeave(Long employeeId, LocalDate asOfDate) {
        // Dummy accrual logic: 2 days per month since hire
        int months = asOfDate.getMonthValue();
        return months * 2;
    }

    public List<LeaveDto> getLeavesForEmployee(Long employeeId) {
        return leaveRequests.getOrDefault(employeeId, Collections.emptyList());
    }

    // Integration with schedule (stub)
    public boolean isLeaveConflictWithSchedule(Long employeeId, LocalDate start, LocalDate end) {
        // TODO: Integrate with ScheduleService
        return false;
    }
}
