package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * SafetyIncident entity for tracking safety incidents in the warehouse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String severity;

    @Size(max = 100)
    private String location;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 20)
    private String status; // OPEN, CLOSED, IN_PROGRESS

    @NotNull
    private LocalDateTime reportedAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
