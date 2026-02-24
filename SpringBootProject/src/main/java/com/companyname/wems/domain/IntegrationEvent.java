package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * IntegrationEvent entity for tracking integration events with external systems.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "integration_events")
public class IntegrationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String eventType;

    @NotBlank
    @Size(max = 255)
    private String payload;

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, PROCESSED, FAILED

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
