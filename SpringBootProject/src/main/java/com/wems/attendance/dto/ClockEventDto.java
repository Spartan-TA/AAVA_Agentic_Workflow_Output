package com.wems.attendance.dto;

import lombok.Data;

@Data
public class ClockEventDto {
    private String badgeId;
    private String deviceId;
    private String location;
    private String latitude;
    private String longitude;
}
