package com.wms.ems.audit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String entity;

    @Column(nullable = false)
    private String operation;

    @Lob
    private String before;

    @Lob
    private String after;

    // Getters and setters omitted for brevity
}
