package com.wms.leave.dtos;

import com.wms.leave.enums.LeaveStatus;
import com.wms.leave.enums.LeaveType;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for LeaveRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String reason;
    private Long approverId;
}
