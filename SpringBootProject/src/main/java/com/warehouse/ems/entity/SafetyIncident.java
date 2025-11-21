package com.warehouse.ems.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private LocalDateTime incidentDate;
    private String severity;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee reportedBy;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Employee getReportedBy() { return reportedBy; }
    public void setReportedBy(Employee reportedBy) { this.reportedBy = reportedBy; }
}