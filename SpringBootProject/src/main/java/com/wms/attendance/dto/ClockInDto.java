package com.wms.attendance.dto;

/**
 * DTO for clock-in/clock-out requests.
 */
public class ClockInDto {
    private Long employeeId;
    private String location;

    public ClockInDto() {}

    public ClockInDto(Long employeeId, String location) {
        this.employeeId = employeeId;
        this.location = location;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
