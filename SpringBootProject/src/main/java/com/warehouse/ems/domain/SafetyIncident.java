package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * SafetyIncident entity for OSHA and safety event tracking.
 */
@Entity
@Table(name = "safety_incident")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private Employee reportedBy;

    private String severity;
    private String location;
    private String description;
    private String status = "OPEN";
    private String investigationNotes;
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
