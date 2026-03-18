package com.warehouse.ems.attendance.dto;

import javax.validation.constraints.NotNull;

/**
 * DTO for clock-out request.
 * Used in POST /api/attendance/clock-out endpoint.
 */
public class ClockOutRequest {

    /** Attendance event ID for clock-out */
    @NotNull(message = "Attendance event ID is required")
    private Long attendanceEventId;

    // Getters and setters
    public Long getAttendanceEventId() {
        return attendanceEventId;
    }

    public void setAttendanceEventId(Long attendanceEventId) {
        this.attendanceEventId = attendanceEventId;
    }
}
