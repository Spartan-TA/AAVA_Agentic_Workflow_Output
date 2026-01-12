package com.warehouseems.audit;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entity;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String actor;

    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String action;

    private String beforeValue;
    private String afterValue;
}
