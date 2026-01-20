package com.wms.leave.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for LeaveRequest entity.
 * Used for REST API requests and responses.
 */
public class LeaveRequestDto implements Serializable {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    @Size(max = 50, message = "Leave type must be less than 50 characters")
    private String leaveType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Status is required")
    @Size(max = 20, message = "Status must be less than 20 characters")
    private String status;

    // Constructors
    public LeaveRequestDto() {}

    public LeaveRequestDto(Long id, Long employeeId, String leaveType, LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Optionally, add mapping from Entity
    public LeaveRequestDto(com.wms.leave.domain.LeaveRequest request) {
        this.id = request.getId();
        this.employeeId = request.getEmployeeId();
        this.leaveType = request.getLeaveType();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.status = request.getStatus();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
