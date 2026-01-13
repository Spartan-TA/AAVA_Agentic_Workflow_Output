package com.wms.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for clock-out requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockOutRequest {
    @NotNull
    private Long employeeId;
    private String location;
    private String deviceId;
}
