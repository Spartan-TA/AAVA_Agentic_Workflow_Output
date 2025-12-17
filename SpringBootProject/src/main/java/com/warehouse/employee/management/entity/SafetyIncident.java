package com.warehouse.employee.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SafetyIncident entity for recording safety events and investigations.
 * Includes audit fields, investigation workflow, and OSHA export fields.
 */
@Entity
@Table(name = "safety_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "reported_by", nullable = false)
    private String reportedBy;

    @NotBlank
    @Column(nullable = false)
    private String severity;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED

    @Column(name = "investigation_notes")
    private String investigationNotes;

    // OSHA export fields can be added here as needed

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
