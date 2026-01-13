package com.warehouse.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
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
    @Size(max = 100)
    private String entity;

    @NotBlank
    @Size(max = 20)
    private String action; // CREATE, UPDATE, DELETE

    @NotBlank
    @Size(max = 50)
    private String actor;

    @NotNull
    private LocalDateTime timestamp;

    @Lob
    private String beforeState;

    @Lob
    private String afterState;

    @Column(name = "tamper_evident_hash")
    private String tamperEvidentHash;
}
