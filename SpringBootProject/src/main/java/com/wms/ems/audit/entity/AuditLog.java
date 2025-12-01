package com.wms.ems.audit.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import com.wms.ems.employee.entity.Employee;

/**
 * AuditLog entity representing system audit logs for actions performed by employees.
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "entity", nullable = false, length = 64)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private Employee performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "details")
    private String details;
}