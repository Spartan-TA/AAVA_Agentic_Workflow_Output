package com.warehouse.employee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employees.
 * Supports soft delete pattern and unique badge ID constraint.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_badge_id", columnList = "badge_id"),
    @Index(name = "idx_department", columnList = "department"),
    @Index(name = "idx_status", columnList = "status")
})
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
     * Used for clock-in/out and system access.
     */
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    private String badgeId;

    /**
     * Full name of the employee.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Employee role: ADMIN, HR, SUPERVISOR, WORKER.
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Department assignment (e.g., Receiving, Shipping, Inventory).
     */
    @Column(length = 100)
    private String department;

    /**
     * Shift group assignment for scheduling.
     */
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    /**
     * Date the employee was hired.
     */
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Current employment status: ACTIVE, ON_LEAVE, TERMINATED.
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Soft delete flag. When true, employee is logically deleted.
     */
    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * Email address for notifications.
     */
    @Column(length = 100)
    private String email;

    /**
     * Phone number for SMS notifications.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Timestamp when the record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User who created the record.
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * User who last updated the record.
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}