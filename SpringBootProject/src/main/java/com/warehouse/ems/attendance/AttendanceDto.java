package com.warehouse.ems.attendance;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Attendance operations.
 * Includes validation annotations for input data.
 */
public class AttendanceDto {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Event date is required")
    @PastOrPresent(message = "Event date cannot be in the future")
    private LocalDate eventDate;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Boolean correctionRequested;
    private Boolean correctionApproved;

    public Long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    public LocalDate getEventDate() {
        return eventDate;
    }
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
    public LocalDateTime getClockIn() {
        return clockIn;
    }
    public void setClockIn(LocalDateTime clockIn) {
        this.clockIn = clockIn;
    }
    public LocalDateTime getClockOut() {
        return clockOut;
    }
    public void setClockOut(LocalDateTime clockOut) {
        this.clockOut = clockOut;
    }
    public Boolean getCorrectionRequested() {
        return correctionRequested;
    }
    public void setCorrectionRequested(Boolean correctionRequested) {
        this.correctionRequested = correctionRequested;
    }
    public Boolean getCorrectionApproved() {
        return correctionApproved;
    }
    public void setCorrectionApproved(Boolean correctionApproved) {
        this.correctionApproved = correctionApproved;
    }
}
