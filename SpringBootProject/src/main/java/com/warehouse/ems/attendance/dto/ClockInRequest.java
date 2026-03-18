package com.warehouse.ems.attendance.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for clock-in request.
 * Used in POST /api/attendance/clock-in endpoint.
 */
public class ClockInRequest {

    /** Employee ID for clock-in */
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    /** Optional notes for clock-in */
    @Size(max = 255, message = "Notes must be at most 255 characters")
    private String notes;

    // Getters and setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
