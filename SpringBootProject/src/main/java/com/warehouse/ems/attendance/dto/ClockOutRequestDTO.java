package com.warehouse.ems.attendance.dto;

import javax.validation.constraints.NotNull;

/**
 * DTO for clock out requests.
 */
public class ClockOutRequestDTO {
    @NotNull
    private Long employeeId;

    // Getters and setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
}
