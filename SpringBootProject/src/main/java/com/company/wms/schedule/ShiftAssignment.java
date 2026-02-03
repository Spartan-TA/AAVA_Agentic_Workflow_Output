package com.company.wms.schedule;

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

    @NotNull
    @Column(name = "employee_id")
    private Long employeeId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id")
    private ShiftTemplate shiftTemplate;

    @NotNull
    @Column(name = "assignment_date")
    private LocalDate assignmentDate;

    @Column(name = "notes")
    private String notes;

    public ShiftAssignment() {}

    public ShiftAssignment(Long employeeId, ShiftTemplate shiftTemplate, LocalDate assignmentDate, String notes) {
        this.employeeId = employeeId;
        this.shiftTemplate = shiftTemplate;
        this.assignmentDate = assignmentDate;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public ShiftTemplate getShiftTemplate() {
        return shiftTemplate;
    }

    public void setShiftTemplate(ShiftTemplate shiftTemplate) {
        this.shiftTemplate = shiftTemplate;
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
