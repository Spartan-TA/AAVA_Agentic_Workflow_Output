package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AuditLog JPA entity.
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

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @NotNull
    private String performedBy;

    private String details;

    public enum AuditAction {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, EXPORT, IMPORT
    }
}
