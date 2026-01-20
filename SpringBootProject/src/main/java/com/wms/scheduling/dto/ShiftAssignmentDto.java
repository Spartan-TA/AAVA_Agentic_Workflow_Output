package com.wms.scheduling.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for ShiftAssignment entity.
 * Used for REST API requests and responses.
 */
public class ShiftAssignmentDto implements Serializable {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Shift template ID is required")
    private Long shiftTemplateId;

    @NotNull(message = "Assignment date is required")
    private LocalDate assignmentDate;

    // Constructors
    public ShiftAssignmentDto() {}

    public ShiftAssignmentDto(Long id, Long employeeId, Long shiftTemplateId, LocalDate assignmentDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.shiftTemplateId = shiftTemplateId;
        this.assignmentDate = assignmentDate;
    }

    // Optionally, add mapping from Entity
    public ShiftAssignmentDto(com.wms.scheduling.domain.ShiftAssignment assignment) {
        this.id = assignment.getId();
        this.employeeId = assignment.getEmployeeId();
        this.shiftTemplateId = assignment.getShiftTemplateId();
        this.assignmentDate = assignment.getAssignmentDate();
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
