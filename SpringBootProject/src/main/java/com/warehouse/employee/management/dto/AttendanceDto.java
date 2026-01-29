package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class AttendanceDto {
    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    @NotNull
    private Long shiftId;

    @DecimalMin("0.0")
    private Double hours;

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
}
