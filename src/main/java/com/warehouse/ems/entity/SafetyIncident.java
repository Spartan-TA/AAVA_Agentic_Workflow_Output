package com.warehouse.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;

    @Column(name = "incident_time", nullable = false)
    private LocalDateTime incidentTime;

    @Column(name = "status", nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED

    // Getters and setters
    // ... (omitted for brevity)
}
