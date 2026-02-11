package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for safety incident reporting.
 */
public class SafetyIncidentDto {
    private Long incidentId;
    @NotNull
    private Long reportedByEmployeeId;
    @NotNull
    private LocalDateTime incidentTime;
    @NotNull
    private String description;
    private String severity;
    private String status;

    public SafetyIncidentDto() {}

    public SafetyIncidentDto(Long incidentId, Long reportedByEmployeeId, LocalDateTime incidentTime, String description, String severity, String status) {
        this.incidentId = incidentId;
        this.reportedByEmployeeId = reportedByEmployeeId;
        this.incidentTime = incidentTime;
        this.description = description;
        this.severity = severity;
        this.status = status;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public Long getReportedByEmployeeId() {
        return reportedByEmployeeId;
    }

    public void setReportedByEmployeeId(Long reportedByEmployeeId) {
        this.reportedByEmployeeId = reportedByEmployeeId;
    }

    public LocalDateTime getIncidentTime() {
        return incidentTime;
    }

    public void setIncidentTime(LocalDateTime incidentTime) {
        this.incidentTime = incidentTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
