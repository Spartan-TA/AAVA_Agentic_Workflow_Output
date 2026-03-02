package com.warehouse.employee.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity for audit trail.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_auditlog_entity_type", columnList = "entityType"),
        @Index(name = "idx_auditlog_entity_id", columnList = "entityId"),
        @Index(name = "idx_auditlog_actor", columnList = "actor")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String entityType;

    @NotNull
    private Long entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Action action;

    @NotBlank
    @Size(max = 100)
    private String actor;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String beforeState;

    @Column(columnDefinition = "TEXT")
    private String afterState;

    @Size(max = 50)
    private String ipAddress;

    @Size(max = 255)
    private String userAgent;

    /**
     * Audit actions.
     */
    public enum Action {
        CREATE, UPDATE, DELETE
    }
}
