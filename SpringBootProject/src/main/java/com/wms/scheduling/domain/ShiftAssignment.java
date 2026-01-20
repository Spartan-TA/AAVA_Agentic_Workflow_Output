package com.wms.scheduling.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing a shift assignment for an employee.
 */
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Shift template ID is required")
    private Long shiftTemplateId;

    @NotNull(message = "Assignment date is required")
    private LocalDate assignmentDate;

    // Constructors
    public ShiftAssignment() {}

    public ShiftAssignment(Long employeeId, Long shiftTemplateId, LocalDate assignmentDate) {
        this.employeeId = employeeId;
        this.shiftTemplateId = shiftTemplateId;
        this.assignmentDate = assignmentDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getShiftTemplateId() { return shiftTemplateId; }
    public void setShiftTemplateId(Long shiftTemplateId) { this.shiftTemplateId = shiftTemplateId; }

    public LocalDate getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }
}
