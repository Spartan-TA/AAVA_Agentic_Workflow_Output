package com.companyname.wem.leave.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import com.companyname.wem.leave.domain.LeaveRequest;
import com.companyname.wem.leave.domain.LeaveStatus;
import com.companyname.wem.leave.dto.LeaveRequestDTO;
import com.companyname.wem.leave.repository.LeaveRequestRepository;
lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRequestRepository repository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequest requestLeave(LeaveRequestDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        LeaveRequest request = LeaveRequest.builder()
            .employee(employee)
            .leaveType(dto.getLeaveType())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .status(LeaveStatus.REQUESTED)
            .comments(dto.getComments())
            .build();
        
        return repository.save(request);
    }

    @Transactional
    public void approveLeave(Long requestId, String approver) {
        LeaveRequest request = repository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        request.setStatus(LeaveStatus.APPROVED);
        request.setApprover(approver);
        repository.save(request);
    }

    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }
}
