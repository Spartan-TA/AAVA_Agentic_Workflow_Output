package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * AuditLog entity for logging all sensitive operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String entity;

    @NotNull
    private Long entityId;

    @NotBlank
    @Size(max = 100)
    private String actor;

    @NotBlank
    @Size(max = 50)
    private String action;

    @Lob
    private String before;

    @Lob
    private String after;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
