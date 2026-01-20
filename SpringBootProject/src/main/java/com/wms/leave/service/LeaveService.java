package com.wms.leave.service;

import com.wms.leave.dto.LeaveRequestDto;
import java.util.List;

/**
 * Service interface for Leave operations.
 * Defines business logic methods for leave management.
 */
public interface LeaveService {
    LeaveRequestDto submitLeaveRequest(LeaveRequestDto leaveRequestDto);
    List<LeaveRequestDto> getLeaveRequestsByEmployeeId(Long employeeId);
    List<LeaveRequestDto> getAllLeaveRequests();
    LeaveRequestDto approveLeave(Long leaveRequestId);
    LeaveRequestDto rejectLeave(Long leaveRequestId);
}
