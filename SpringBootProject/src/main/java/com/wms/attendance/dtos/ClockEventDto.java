package com.wms.attendance.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for clock-in/clock-out events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockEventDto {
    private Long employeeId;
    private String employeeName;
    private LocalDateTime eventTime;
    private String eventType; // CLOCK_IN or CLOCK_OUT
    private String remarks;
}
