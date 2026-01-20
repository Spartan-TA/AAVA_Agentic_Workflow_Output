package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Incident JPA entity.
 */
@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    private LocalDateTime incidentDate;

    @NotBlank
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IncidentType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    public enum IncidentType {
        SAFETY, SECURITY, EQUIPMENT, OTHER
    }

    public enum IncidentSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum IncidentStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
