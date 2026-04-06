package com.warehouse.ems.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SafetyIncidentDTO {
    private Long id;
    private String severity;
    private String location;
    private String description;
    private List<Long> involvedEmployees;
    private String status;
    private LocalDateTime reportedDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Long> getInvolvedEmployees() { return involvedEmployees; }
    public void setInvolvedEmployees(List<Long> involvedEmployees) { this.involvedEmployees = involvedEmployees; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDateTime reportedDate) { this.reportedDate = reportedDate; }
}
