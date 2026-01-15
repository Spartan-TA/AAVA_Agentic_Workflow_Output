package com.warehouse.audit.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

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
    private String action;

    @NotBlank
    private String entity;

    @NotNull
    private Long entityId;

    @NotBlank
    private String username;

    @NotNull
    private LocalDateTime timestamp;

    @NotBlank
    private String details;
}
