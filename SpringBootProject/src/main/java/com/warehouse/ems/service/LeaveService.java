package com.warehouse.ems.service;

import com.warehouse.ems.dto.LeaveRequestDto;
import java.util.List;

public interface LeaveService {
    LeaveRequestDto getLeaveRequestById(Long id);
    List<LeaveRequestDto> getAllLeaveRequests();
    LeaveRequestDto createLeaveRequest(LeaveRequestDto leaveRequestDto);
    LeaveRequestDto approveLeaveRequest(Long id);
    void deleteLeaveRequest(Long id);
}
