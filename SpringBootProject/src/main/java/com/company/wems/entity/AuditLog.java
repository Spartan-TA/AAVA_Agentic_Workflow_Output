package com.company.wems.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entity for compliance and tracking.
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

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "details")
    private String details;
}
