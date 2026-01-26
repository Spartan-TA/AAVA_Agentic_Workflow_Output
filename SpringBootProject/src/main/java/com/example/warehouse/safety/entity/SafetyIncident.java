package com.example.warehouse.safety.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDateTime incidentTime;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String severity; // e.g., LOW, MEDIUM, HIGH

    @Column
    private String correctiveAction;

    // Constructors
    public SafetyIncident() {}

    public SafetyIncident(Long employeeId, LocalDateTime incidentTime, String description, String severity, String correctiveAction) {
        this.employeeId = employeeId;
        this.incidentTime = incidentTime;
        this.description = description;
        this.severity = severity;
        this.correctiveAction = correctiveAction;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getIncidentTime() { return incidentTime; }
    public void setIncidentTime(LocalDateTime incidentTime) { this.incidentTime = incidentTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
}
