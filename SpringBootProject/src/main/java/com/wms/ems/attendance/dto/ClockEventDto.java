package com.wms.ems.attendance.dto;

import lombok.Data;

@Data
public class ClockEventDto {
    private Long employeeId;
    private String deviceId;
    private String location;
    private Long shiftId;
}
