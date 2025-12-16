package com.company.warehouse.attendance.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for attendance reporting and export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDto {
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private double hoursWorked;
    private String status;
}
