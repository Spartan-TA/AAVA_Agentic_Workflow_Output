// src/main/java/com/company/warehouse/audit/AuditLog.java
package com.company.warehouse.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for audit logging of sensitive changes.
 */
@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;
    private String action;
    private String entity;
    @Column(columnDefinition = "TEXT")
    private String beforeState;
    @Column(columnDefinition = "TEXT")
    private String afterState;
    private LocalDateTime timestamp;
}