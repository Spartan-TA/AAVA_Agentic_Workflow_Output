package com.warehouse.ems.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for tracking actions in the system.
 */
@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "action", nullable = false)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private Employee performedBy;

    @NotBlank
    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "details")
    private String details;
}
