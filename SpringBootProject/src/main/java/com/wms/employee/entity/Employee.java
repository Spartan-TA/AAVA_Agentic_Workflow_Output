package com.wms.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employee master data.
 */
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 64)
    private String role;

    @Column(nullable = false, length = 64)
    private String department;

    @Column(length = 64)
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private boolean softDelete = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 64)
    private String createdBy;

    @Column(length = 64)
    private String updatedBy;

    // Getters and setters omitted for brevity
    // Add Lombok @Data or manually implement if preferred
}
