package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity for immutable audit trail.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String actor;

    @Column(nullable = false, length = 64)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "immutable")
    private Boolean immutable = true;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}
