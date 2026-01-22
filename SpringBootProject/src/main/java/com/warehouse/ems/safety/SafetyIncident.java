package com.warehouse.ems.safety;

import com.warehouse.ems.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a safety incident for OSHA reporting.
 */
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Severity is required.")
    @Column(nullable = false)
    private Severity severity;

    @NotNull(message = "Location is required.")
    @Column(nullable = false)
    private String location;

    @Lob
    @NotNull(message = "Description is required.")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required.")
    @Column(nullable = false)
    private Status status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> involvedEmployees = new HashSet<>();

    @NotNull(message = "Incident date is required.")
    @Column(nullable = false)
    private LocalDateTime incidentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    @NotNull(message = "Reported by is required.")
    private Employee reportedBy;

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN, INVESTIGATING, RESOLVED
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Set<Employee> getInvolvedEmployees() { return involvedEmployees; }
    public void setInvolvedEmployees(Set<Employee> involvedEmployees) { this.involvedEmployees = involvedEmployees; }
    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }
    public Employee getReportedBy() { return reportedBy; }
    public void setReportedBy(Employee reportedBy) { this.reportedBy = reportedBy; }
}
