package com.warehouse.ems.attendance.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for AttendanceEvent entity.
 * Used for returning attendance event data to clients.
 */
public class AttendanceDto {

    /** Attendance event unique identifier */
    private Long id;

    /** Employee ID associated with the attendance event */
    @NotNull
    private Long employeeId;

    /** Clock-in timestamp */
    @NotNull
    private LocalDateTime clockInTime;

    /** Clock-out timestamp */
    private LocalDateTime clockOutTime;

    /** Attendance status (e.g., PRESENT, ABSENT, LATE) */
    @NotNull
    @Size(min = 2, max = 20)
    private String status;

    /** Notes or remarks for the attendance event */
    @Size(max = 255)
    private String notes;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
