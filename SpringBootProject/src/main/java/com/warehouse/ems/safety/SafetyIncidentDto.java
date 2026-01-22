package com.warehouse.ems.safety;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for SafetyIncident with validation annotations.
 */
public class SafetyIncidentDto {
    private Long id;

    @NotNull(message = "Severity is required.")
    private String severity;

    @NotNull(message = "Location is required.")
    @Size(min = 2, max = 128, message = "Location must be between 2 and 128 characters.")
    private String location;

    @NotNull(message = "Description is required.")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters.")
    private String description;

    @NotNull(message = "Status is required.")
    private String status;

    private Set<Long> involvedEmployeeIds;

    @NotNull(message = "Incident date is required.")
    private LocalDateTime incidentDate;

    @NotNull(message = "Reported by employee ID is required.")
    private Long reportedById;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<Long> getInvolvedEmployeeIds() { return involvedEmployeeIds; }
    public void setInvolvedEmployeeIds(Set<Long> involvedEmployeeIds) { this.involvedEmployeeIds = involvedEmployeeIds; }
    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }
    public Long getReportedById() { return reportedById; }
    public void setReportedById(Long reportedById) { this.reportedById = reportedById; }
}
