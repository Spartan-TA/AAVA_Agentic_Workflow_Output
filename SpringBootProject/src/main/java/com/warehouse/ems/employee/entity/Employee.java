package com.warehouse.ems.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse staff.
 * Includes audit fields and soft-delete flag.
 */
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "shift_group", length = 32)
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and setters omitted for brevity
    // Add Lombok @Data or manually implement if preferred
}
