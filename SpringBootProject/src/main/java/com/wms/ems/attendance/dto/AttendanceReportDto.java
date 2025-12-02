package com.wms.ems.attendance.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceReportDto {
    private Long employeeId;
    private LocalDate date;
    private double totalHours;
    private int missedPunches;
}
