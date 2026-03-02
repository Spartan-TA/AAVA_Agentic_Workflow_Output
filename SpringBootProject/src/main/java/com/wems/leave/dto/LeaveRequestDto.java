package com.wems.leave.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
}
