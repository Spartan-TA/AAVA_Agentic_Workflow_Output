package com.warehouse.ems.scheduling;

import com.warehouse.ems.common.BaseEntity;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing the assignment of a shift to an employee.
 */
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment extends BaseEntity {

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @NotNull
    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "notes")
    private String notes;

    public ShiftAssignment() {
    }

    public ShiftAssignment(Long employeeId, Long shiftId, LocalDate assignmentDate, String notes) {
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
