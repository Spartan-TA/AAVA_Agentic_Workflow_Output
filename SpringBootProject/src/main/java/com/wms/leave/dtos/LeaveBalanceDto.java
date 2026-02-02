package com.wms.leave.dtos;

import com.wms.leave.enums.LeaveType;
import lombok.*;

/**
 * Data Transfer Object for LeaveBalance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDto {
    private Long id;
    private Long employeeId;
    private LeaveType leaveType;
    private Double balance;
}
