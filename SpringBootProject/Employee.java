package com.wms.employee;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employees.
 * Supports soft delete and audit timestamps.
 * 
 * Key Features:
 * - Unique badge ID enforcement
 * - Soft delete for data retention
 * - Automatic timestamp management
 * - Status tracking (ACTIVE, INACTIVE, ON_LEAVE, TERMINATED)
 */
@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique badge identifier for the employee.
     * Used for clock-in/out and access control.
     */
    @Column(unique = true, nullable = false, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 128)
    private String name;

    /**
     * Employee role (e.g., WORKER, SUPERVISOR, MANAGER).
     */
    @Column(length = 32)
    private String role;

    @Column(length = 64)
    private String department;

    /**
     * Shift group assignment (e.g., A, B, C for rotating shifts).
     */
    @Column(length = 32)
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    /**
     * Current employment status.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private EmployeeStatus status;

    /**
     * Soft delete flag. When true, employee is hidden from active queries.
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    /**
     * Timestamp when the employee record was created.
     * Automatically set on persist.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the employee record was last updated.
     * Automatically updated on every save.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback to set creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback to update modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
