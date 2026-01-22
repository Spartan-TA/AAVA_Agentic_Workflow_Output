package com.wms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SafetyIncident extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime incidentTime;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String severity;

    // Getters and setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDateTime getIncidentTime() { return incidentTime; }
    public void setIncidentTime(LocalDateTime incidentTime) { this.incidentTime = incidentTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
