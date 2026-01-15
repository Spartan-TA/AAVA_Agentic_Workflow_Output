package com.warehouse.leave.dto;

import com.warehouse.leave.entity.LeaveRequest;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveDTO {
    private Long id;
    private LeaveRequest.LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveRequest.LeaveStatus status;
    private Double balance;
    private Long employeeId;
}
