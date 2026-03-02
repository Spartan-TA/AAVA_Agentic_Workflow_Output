package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SafetyIncident entity for safety incident tracking.
 */
@Entity
@Table(name = "safety_incidents", indexes = {
        @Index(name = "idx_safetyincident_status", columnList = "status"),
        @Index(name = "idx_safetyincident_severity", columnList = "severity"),
        @Index(name = "idx_safetyincident_incident_date", columnList = "incidentDate"),
        @Index(name = "idx_safetyincident_location", columnList = "location")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee who reported the incident.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    @JsonIgnore
    private Employee reportedBy;

    @NotNull
    private LocalDateTime incidentDate;

    @NotBlank
    @Size(max = 150)
    private String location;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @NotBlank
    @Size(max = 1000)
    private String description;

    /**
     * Employees involved in the incident.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "safety_incident_involved_employees",
            joinColumns = @JoinColumn(name = "safety_incident_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @JsonIgnore
    private List<Employee> involvedEmployees;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @Size(max = 1000)
    private String investigationNotes;

    @Size(max = 1000)
    private String correctiveActions;

    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Severity levels.
     */
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Incident status.
     */
    public enum Status {
        OPEN, INVESTIGATING, RESOLVED
    }
}
