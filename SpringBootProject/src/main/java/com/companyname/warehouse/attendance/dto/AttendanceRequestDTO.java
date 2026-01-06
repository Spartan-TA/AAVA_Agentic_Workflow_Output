package com.companyname.warehouse.attendance.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for attendance clock-in/out requests.
 */
@Data
public class AttendanceRequestDTO {
    @NotNull
    private Long employeeId;
    @NotNull
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String location;
    private String notes;
}
