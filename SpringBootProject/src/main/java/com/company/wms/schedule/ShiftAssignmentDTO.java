package com.company.wms.schedule;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object for Shift Assignment information.
 */
public class ShiftAssignmentDTO {

    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long shiftTemplateId;

    @NotNull
    private LocalDate assignmentDate;

    private String notes;

    public ShiftAssignmentDTO() {}

    public ShiftAssignmentDTO(Long id, Long employeeId, Long shiftTemplateId, LocalDate assignmentDate, String notes) {
        this.id = id;
        this.employeeId = employeeId;
        this.shiftTemplateId = shiftTemplateId;
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

    public Long getShiftTemplateId() {
        return shiftTemplateId;
    }

    public void setShiftTemplateId(Long shiftTemplateId) {
        this.shiftTemplateId = shiftTemplateId;
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
