package com.warehouse.ems.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a safety incident reported in the warehouse.
 */
@Entity
@Table(name = "safety_incident")
@Data
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private Employee reportedBy;

    @NotBlank
    @Column(name = "incident_type", nullable = false)
    private String incidentType;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
