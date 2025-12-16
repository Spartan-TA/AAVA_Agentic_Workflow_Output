package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity for compliance and change tracking.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;
    private String entity;
    private Long entityId;
    private String action;
    private String beforeState;
    private String afterState;
    private LocalDateTime timestamp;

    // Getters and setters omitted for brevity
}
