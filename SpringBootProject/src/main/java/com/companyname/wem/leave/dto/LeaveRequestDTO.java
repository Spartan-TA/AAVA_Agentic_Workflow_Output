package com.companyname.wem.leave.dto;

import com.companyname.wem.leave.domain.LeaveStatus;
import com.companyname.wem.leave.domain.LeaveType;
import jakarta.validation.constraints.NotNull;
lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    private Long id;
    
    @NotNull
    private Long employeeId;
    
    @NotNull
    private LeaveType leaveType;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private LeaveStatus status;
    private String approver;
    private String comments;
}
