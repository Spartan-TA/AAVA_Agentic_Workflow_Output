package com.warehouse.employee.dto;

import java.time.LocalDate;

/**
 * DTO for shift assignment response.
 */
public class ShiftAssignmentResponse {
    private Long assignmentId;
    private Long employeeId;
    private Long shiftId;
    private LocalDate assignmentDate;
    private String shiftName;

    public ShiftAssignmentResponse() {}

    public ShiftAssignmentResponse(Long assignmentId, Long employeeId, Long shiftId, LocalDate assignmentDate, String shiftName) {
        this.assignmentId = assignmentId;
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.assignmentDate = assignmentDate;
        this.shiftName = shiftName;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }
}
