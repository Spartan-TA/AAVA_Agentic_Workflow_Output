package com.wms.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for clock-in requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockInRequest {
    @NotNull
    private Long employeeId;
    private String location;
    private String deviceId;
}
