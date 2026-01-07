package com.company.warehouse.core.domain;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry.
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

    @Column(name = "entity_name", nullable = false)
    @NotBlank
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    @NotNull
    private Long entityId;

    @Column(name = "action", nullable = false)
    @NotBlank
    private String action;

    @Column(name = "performed_by", nullable = false)
    @NotBlank
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    @NotNull
    private LocalDateTime performedAt;

    @Column(name = "details")
    private String details;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
