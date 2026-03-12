package com.company.wems.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_employee_id", nullable = false)
    private Employee reportedBy;

    @Column(name = "severity")
    private String severity;

    @Column(name = "status")
    private String status;

    // Constructors, getters, setters
    public SafetyIncident() {}

    public SafetyIncident(String title, String description, LocalDateTime incidentDate, Employee reportedBy, String severity, String status) {
        this.title = title;
        this.description = description;
        this.incidentDate = incidentDate;
        this.reportedBy = reportedBy;
        this.severity = severity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDateTime incidentDate) {
        this.incidentDate = incidentDate;
    }

    public Employee getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Employee reportedBy) {
        this.reportedBy = reportedBy;
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
