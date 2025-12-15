package com.warehouseems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * DTO for clock-out requests.
 */
@Data
public class ClockOutDto {
    @Schema(description = "Employee badge ID", required = true)
    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @Schema(description = "Device ID used for clock-out")
    private String deviceId;

    @Schema(description = "Location of clock-out")
    private String location;

    @Schema(description = "IP address of clock-out")
    private String ipAddress;
}
