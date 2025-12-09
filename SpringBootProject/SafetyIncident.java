package com.example.warehousemanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a Safety Incident in the warehouse.
 */
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {

    public enum Severity {
        MINOR, MODERATE, SEVERE, FATAL
    }

    public enum WorkflowStatus {
        OPEN, INVESTIGATING, RESOLVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", nullable = false)
    private WorkflowStatus workflowStatus;

    @Column(nullable = false)
    private String location;

    @Column(length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> involvedEmployees;

    @Column(name = "incident_datetime", nullable = false)
    private LocalDateTime incidentDateTime;

    @ElementCollection
    @CollectionTable(name = "incident_attachments", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "attachment_url")
    private List<String> attachments;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public WorkflowStatus getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(WorkflowStatus workflowStatus) { this.workflowStatus = workflowStatus; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Employee> getInvolvedEmployees() { return involvedEmployees; }
    public void setInvolvedEmployees(List<Employee> involvedEmployees) { this.involvedEmployees = involvedEmployees; }

    public LocalDateTime getIncidentDateTime() { return incidentDateTime; }
    public void setIncidentDateTime(LocalDateTime incidentDateTime) { this.incidentDateTime = incidentDateTime; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}
