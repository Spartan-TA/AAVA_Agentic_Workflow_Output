package com.warehouseems.attendance.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Attendance API requests and responses.
 */
public class AttendanceDto {
    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    private String type; // IN or OUT

    private String deviceId;
    private String location;

    // Getters and setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}