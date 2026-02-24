package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * CorrectiveAction entity for tracking corrective actions related to safety incidents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "corrective_actions")
public class CorrectiveAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long safetyIncidentId;

    @NotBlank
    @Size(max = 255)
    private String actionDescription;

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, COMPLETED

    @NotNull
    private LocalDateTime dueDate;

    private LocalDateTime completedAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
