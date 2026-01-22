package com.warehouse.ems.scheduling;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object for ShiftAssignment.
 */
public class ShiftDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Shift ID is required")
    private Long shiftId;

    @NotNull(message = "Assignment date is required")
    private LocalDate assignmentDate;

    private String notes;

    public ShiftDto() {
    }

    public ShiftDto(Long employeeId, Long shiftId, LocalDate assignmentDate, String notes) {
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.assignmentDate = assignmentDate;
        this.notes = notes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
