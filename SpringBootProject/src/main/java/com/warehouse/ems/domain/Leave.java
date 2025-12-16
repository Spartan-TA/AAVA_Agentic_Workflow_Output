package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave entity for PTO, sick, and unpaid leave management.
 */
@Entity
@Table(name = "leave")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status = "PENDING";
    private Double accrualBalance = 0.0;
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}
