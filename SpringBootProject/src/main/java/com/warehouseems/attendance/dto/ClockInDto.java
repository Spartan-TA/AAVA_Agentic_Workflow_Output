package com.warehouseems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * DTO for clock-in requests.
 */
@Data
public class ClockInDto {
    @Schema(description = "Employee badge ID", required = true)
    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @Schema(description = "Device ID used for clock-in")
    private String deviceId;

    @Schema(description = "Location of clock-in")
    private String location;

    @Schema(description = "IP address of clock-in")
    private String ipAddress;
}
