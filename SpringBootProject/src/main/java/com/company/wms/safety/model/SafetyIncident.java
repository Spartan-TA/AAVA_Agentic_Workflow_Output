package com.company.wms.safety.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a safety incident reported in the warehouse.
 */
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title or short description of the incident.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of the incident.
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Date and time when the incident occurred.
     */
    @Column(name = "incident_time", nullable = false)
    private LocalDateTime incidentTime;

    /**
     * The employee who reported the incident.
     */
    @Column(name = "reported_by", nullable = false)
    private Long reportedByEmployeeId;

    /**
     * Status of the incident (e.g., OPEN, INVESTIGATING, CLOSED).
     */
    @Column(nullable = false)
    private String status;

    // Constructors, getters, setters, equals, hashCode, toString

    public SafetyIncident() {}

    public SafetyIncident(String title, String description, LocalDateTime incidentTime, Long reportedByEmployeeId, String status) {
        this.title = title;
        this.description = description;
        this.incidentTime = incidentTime;
        this.reportedByEmployeeId = reportedByEmployeeId;
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

    public LocalDateTime getIncidentTime() {
        return incidentTime;
    }

    public void setIncidentTime(LocalDateTime incidentTime) {
        this.incidentTime = incidentTime;
    }

    public Long getReportedByEmployeeId() {
        return reportedByEmployeeId;
    }

    public void setReportedByEmployeeId(Long reportedByEmployeeId) {
        this.reportedByEmployeeId = reportedByEmployeeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SafetyIncident that = (SafetyIncident) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "SafetyIncident{" +
                "id=" + id +
                ", title='" + title + ''' +
                ", description='" + description + ''' +
                ", incidentTime=" + incidentTime +
                ", reportedByEmployeeId=" + reportedByEmployeeId +
                ", status='" + status + ''' +
                '}';
    }
}
