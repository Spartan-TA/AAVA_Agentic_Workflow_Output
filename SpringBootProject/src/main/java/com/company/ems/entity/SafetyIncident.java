package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * SafetyIncident entity for OSHA reporting.
 */
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    @Column(name = "severity", nullable = false)
    private String severity;

    @NotBlank
    @Column(name = "location", nullable = false)
    private String location;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "incident_time", nullable = false)
    private LocalDateTime incidentTime;

    @Column(name = "status")
    private String status;

    // Getters and setters omitted for brevity
}
