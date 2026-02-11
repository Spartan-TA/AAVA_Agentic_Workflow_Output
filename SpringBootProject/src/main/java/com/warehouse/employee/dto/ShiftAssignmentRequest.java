package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for shift assignment request.
 */
public class ShiftAssignmentRequest {
    @NotNull
    private Long employeeId;

    @NotNull
    private Long shiftId;

    @NotNull
    private LocalDate assignmentDate;

    public ShiftAssignmentRequest() {}

    public ShiftAssignmentRequest(Long employeeId, Long shiftId, LocalDate assignmentDate) {
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.assignmentDate = assignmentDate;
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
}
