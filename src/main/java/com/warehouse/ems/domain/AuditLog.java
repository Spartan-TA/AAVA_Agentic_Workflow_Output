package com.warehouse.ems.domain;

import com.warehouse.ems.enums.AuditAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for sensitive changes.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @NotBlank
    private String entity;

    private String entityId;

    private String before;
    private String after;

    @NotNull
    private LocalDateTime timestamp;
}
