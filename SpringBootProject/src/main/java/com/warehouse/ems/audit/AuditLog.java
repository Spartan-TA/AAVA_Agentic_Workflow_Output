package com.warehouse.ems.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for entity changes.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "entity", nullable = false)
    private String entity;

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @NotBlank
    @Column(name = "actor", nullable = false)
    private String actor;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "before", columnDefinition = "TEXT")
    private String before;

    @Column(name = "after", columnDefinition = "TEXT")
    private String after;

    @NotBlank
    @Column(name = "action", nullable = false)
    private String action;
}
