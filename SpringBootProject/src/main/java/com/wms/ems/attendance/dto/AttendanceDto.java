package com.wms.ems.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceDto {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String date; // ISO format yyyy-MM-dd

    @NotBlank
    private String time; // ISO format HH:mm:ss

    private String location;
    private String deviceId;
}
