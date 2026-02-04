package com.company.warehouse.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for audit logging.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;

    private String entity;

    private String action;

    @Column(columnDefinition = "TEXT")
    private String before;

    @Column(columnDefinition = "TEXT")
    private String after;

    private LocalDateTime timestamp;
}