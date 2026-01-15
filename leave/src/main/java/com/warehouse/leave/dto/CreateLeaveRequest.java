package com.warehouse.leave.dto;

import com.warehouse.leave.entity.LeaveRequest;
import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeaveRequest {
    @NotNull
    private LeaveRequest.LeaveType type;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private Long employeeId;
}
