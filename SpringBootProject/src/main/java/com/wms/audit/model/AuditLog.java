package com.wms.audit.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry.
 */
@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Entity name (e.g., Employee, Schedule) */
    @Column(nullable = false)
    private String entity;

    /** Entity ID */
    @Column(nullable = false)
    private Long entityId;

    /** Action performed (e.g., CREATE, UPDATE, DELETE) */
    @Column(nullable = false)
    private String action;

    /** Actor (username or system) */
    @Column(nullable = false)
    private String actor;

    /** Timestamp of the action */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** State before the change (JSON or text) */
    @Lob
    private String beforeState;

    /** State after the change (JSON or text) */
    @Lob
    private String afterState;
}
