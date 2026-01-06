package com.company.wms.safety.model;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a corrective action taken in response to a safety incident.
 */
@Entity
@Table(name = "corrective_actions")
public class CorrectiveAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The safety incident this corrective action addresses.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private SafetyIncident safetyIncident;

    /**
     * Description of the corrective action.
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Date the corrective action was assigned.
     */
    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    /**
     * Date the corrective action was completed.
     */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    /**
     * The employee responsible for the corrective action.
     */
    @Column(name = "responsible_employee_id")
    private Long responsibleEmployeeId;

    // Constructors, getters, setters, equals, hashCode, toString

    public CorrectiveAction() {}

    public CorrectiveAction(SafetyIncident safetyIncident, String description, LocalDate assignedDate, LocalDate completedDate, Long responsibleEmployeeId) {
        this.safetyIncident = safetyIncident;
        this.description = description;
        this.assignedDate = assignedDate;
        this.completedDate = completedDate;
        this.responsibleEmployeeId = responsibleEmployeeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SafetyIncident getSafetyIncident() {
        return safetyIncident;
    }

    public void setSafetyIncident(SafetyIncident safetyIncident) {
        this.safetyIncident = safetyIncident;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public Long getResponsibleEmployeeId() {
        return responsibleEmployeeId;
    }

    public void setResponsibleEmployeeId(Long responsibleEmployeeId) {
        this.responsibleEmployeeId = responsibleEmployeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorrectiveAction that = (CorrectiveAction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CorrectiveAction{" +
                "id=" + id +
                ", safetyIncident=" + (safetyIncident != null ? safetyIncident.getId() : null) +
                ", description='" + description + ''' +
                ", assignedDate=" + assignedDate +
                ", completedDate=" + completedDate +
                ", responsibleEmployeeId=" + responsibleEmployeeId +
                '}';
    }
}
