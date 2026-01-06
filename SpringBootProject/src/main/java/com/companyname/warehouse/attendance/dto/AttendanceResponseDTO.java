package com.companyname.warehouse.attendance.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for attendance API responses.
 */
@Data
public class AttendanceResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String location;
    private String notes;
}
