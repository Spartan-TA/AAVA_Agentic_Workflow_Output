package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for clock-in request.
 */
public class ClockInRequest {
    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime clockInTime;

    public ClockInRequest() {}

    public ClockInRequest(Long employeeId, LocalDateTime clockInTime) {
        this.employeeId = employeeId;
        this.clockInTime = clockInTime;
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
}
