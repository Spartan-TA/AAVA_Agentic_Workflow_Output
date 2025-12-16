package com.company.warehouse.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for attendance clock-in/out requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockEventDto {
    @NotNull
    private Long employeeId;
    private String deviceId;
    private String location;
}
