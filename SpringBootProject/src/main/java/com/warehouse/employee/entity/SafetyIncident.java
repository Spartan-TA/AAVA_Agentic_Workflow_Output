package com.warehouse.employee.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "safety_incident")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String description;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    private String severity;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}