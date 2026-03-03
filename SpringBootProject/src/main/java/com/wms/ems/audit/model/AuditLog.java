package com.wms.ems.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable entity representing an audit log entry.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * Primary key for the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the entity being audited.
     */
    @Column(nullable = false)
    private String entity;

    /**
     * ID of the entity being audited.
     */
    private Long entityId;

    /**
     * Action performed (e.g., CREATE, UPDATE, DELETE).
     */
    @Column(nullable = false)
    private String action;

    /**
     * Actor who performed the action.
     */
    private String actor;

    /**
     * Timestamp of the audit log entry.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * State of the entity before the action.
     */
    @Column(length = 5000)
    private String beforeState;

    /**
     * State of the entity after the action.
     */
    @Column(length = 5000)
    private String afterState;
}