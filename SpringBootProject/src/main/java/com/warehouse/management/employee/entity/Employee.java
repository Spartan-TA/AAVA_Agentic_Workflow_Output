package com.warehouse.management.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a warehouse employee.
 * 
 * This entity stores all employee master data including personal information,
 * role assignments, department affiliations, and employment status.
 * Implements soft-delete pattern to preserve historical data.
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employee", indexes = {
    @Index(name = "idx_employee_badge_id", columnList = "badge_id"),
    @Index(name = "idx_employee_department", columnList = "department"),
    @Index(name = "idx_employee_role", columnList = "role"),
    @Index(name = "idx_employee_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    /**
     * Unique identifier for the employee record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique badge ID for the employee.
     * Used for physical access control and time tracking.
     */
    @Column(name = "badge_id", nullable = false, unique = true, length = 32)
    private String badgeId;

    /**
     * Full name of the employee.
     */
    @Column(nullable = false, length = 128)
    private String name;

    /**
     * Role of the employee in the system.
     * Valid values: ADMIN, HR, SUPERVISOR, WORKER
     */
    @Column(nullable = false, length = 32)
    private String role;

    /**
     * Department where the employee works.
     * Examples: Shipping, Receiving, Inventory, Quality Control
     */
    @Column(length = 64)
    private String department;

    /**
     * Shift group assignment for the employee.
     * Examples: A, B, C (for rotating shifts)
     */
    @Column(name = "shift_group", length = 32)
    private String shiftGroup;

    /**
     * Date when the employee was hired.
     */
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Current employment status.
     * Valid values: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
     */
    @Column(nullable = false, length = 16)
    private String status;

    /**
     * Soft-delete flag.
     * When true, the employee is considered deleted but data is preserved.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Timestamp when the record was created.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User who created the record.
     */
    @CreatedBy
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * User who last updated the record.
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * Pre-persist callback to set default values.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (deleted == null) {
            deleted = false;
        }
    }

    /**
     * Pre-update callback to update timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}