package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Certification entity for training and compliance tracking.
 */
@Entity
@Table(name = "certification")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
    private String status = "ACTIVE";
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
