package com.warehouse.employee.dto;

import java.time.LocalDateTime;

/**
 * DTO for attendance response.
 */
public class AttendanceResponse {
    private Long attendanceId;
    private Long employeeId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String status;

    public AttendanceResponse() {}

    public AttendanceResponse(Long attendanceId, Long employeeId, LocalDateTime clockInTime, LocalDateTime clockOutTime, String status) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.status = status;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
