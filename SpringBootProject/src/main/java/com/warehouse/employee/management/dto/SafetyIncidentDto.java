package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.time.LocalDate;

public class SafetyIncidentDto {
    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String severity;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    private String status;

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
