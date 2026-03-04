package com.warehouse.ems.attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "DTO for Attendance entity")
public class AttendanceDTO {
    @Schema(description = "Attendance record ID")
    private Long id;

    @Schema(description = "Employee ID")
    private Long employeeId;

    @Schema(description = "Shift ID")
    private Long shiftId;

    @NotNull
    @Schema(description = "Clock-in timestamp")
    private LocalDateTime clockIn;

    @Schema(description = "Clock-out timestamp")
    private LocalDateTime clockOut;

    @Schema(description = "Device ID used for clock-in/out")
    private String deviceId;

    @Schema(description = "Geolocation at clock-in/out (lat,long)")
    private String geolocation;

    @Schema(description = "Total hours worked for this attendance record")
    private Double hoursWorked;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getGeolocation() { return geolocation; }
    public void setGeolocation(String geolocation) { this.geolocation = geolocation; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
}
