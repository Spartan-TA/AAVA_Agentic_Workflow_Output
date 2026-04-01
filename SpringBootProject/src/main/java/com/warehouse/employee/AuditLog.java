package com.warehouse.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity for audit trail and compliance.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Column(name = "before_state")
    private String beforeState;

    @Column(name = "after_state")
    private String afterState;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Getters and setters omitted for brevity
}
