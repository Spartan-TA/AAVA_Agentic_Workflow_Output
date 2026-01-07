package com.company.warehouse.core.domain;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entity representing a safety incident in the warehouse.
 */
@Entity
@Table(name = "safety_incident")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_time", nullable = false)
    @NotNull
    private LocalDateTime incidentTime;

    @Column(name = "description", nullable = false)
    @NotBlank
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    @NotNull
    private Employee reportedBy;

    @Column(name = "severity", nullable = false)
    @NotBlank
    private String severity;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
