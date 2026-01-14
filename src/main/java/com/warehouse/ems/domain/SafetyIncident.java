package com.warehouse.ems.domain;

import com.warehouse.ems.enums.Severity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a safety incident or near-miss.
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @NotBlank
    private String location;

    @NotBlank
    private String description;

    @NotNull
    private LocalDateTime incidentTime;

    private String status;
}
