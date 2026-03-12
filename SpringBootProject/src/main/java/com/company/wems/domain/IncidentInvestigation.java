package com.company.wems.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_investigations")
public class IncidentInvestigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private SafetyIncident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigator_id", nullable = false)
    private Employee investigator;

    @Column(name = "investigation_date", nullable = false)
    private LocalDateTime investigationDate;

    @Column(name = "findings")
    private String findings;

    @Column(name = "actions_taken")
    private String actionsTaken;

    // Constructors, getters, setters
    public IncidentInvestigation() {}

    public IncidentInvestigation(SafetyIncident incident, Employee investigator, LocalDateTime investigationDate, String findings, String actionsTaken) {
        this.incident = incident;
        this.investigator = investigator;
        this.investigationDate = investigationDate;
        this.findings = findings;
        this.actionsTaken = actionsTaken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SafetyIncident getIncident() {
        return incident;
    }

    public void setIncident(SafetyIncident incident) {
        this.incident = incident;
    }

    public Employee getInvestigator() {
        return investigator;
    }

    public void setInvestigator(Employee investigator) {
        this.investigator = investigator;
    }

    public LocalDateTime getInvestigationDate() {
        return investigationDate;
    }

    public void setInvestigationDate(LocalDateTime investigationDate) {
        this.investigationDate = investigationDate;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }
}
