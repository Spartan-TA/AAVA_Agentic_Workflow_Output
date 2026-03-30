package com.companyname.wems.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employee master data
 * 
 * This entity serves as the single source of truth for employee information
 * across the warehouse management system. It includes:
 * - Basic employee information (name, badge ID)
 * - Role and department assignment
 * - Shift group for scheduling
 * - Employment status tracking
 * - Audit timestamps
 * 
 * The badgeId field is unique and used for clock-in/out operations
 * Status field supports soft-delete functionality
 * 
 * @author WEMS Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employees", 
       uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"),
       indexes = {
           @Index(name = "idx_badge_id", columnList = "badge_id"),
           @Index(name = "idx_department", columnList = "department"),
           @Index(name = "idx_status", columnList = "status")
       })
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Employee {
    
    /**
     * Primary key - auto-generated employee ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee full name
     * Required field with max length of 100 characters
     */
    @NotNull(message = "Employee name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique badge identifier for employee
     * Used for clock-in/out and access control
     * Format: Alphanumeric, 5-20 characters
     */
    @NotNull(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Badge ID must be 5-20 alphanumeric characters")
    @Column(name = "badge_id", unique = true, nullable = false, length = 20)
    private String badgeId;

    /**
     * Employee role for RBAC
     * Determines access permissions and capabilities
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Department assignment
     * Used for filtering and reporting
     */
    @NotNull(message = "Department is required")
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * Shift group for scheduling
     * Determines default shift assignments
     */
    @NotNull(message = "Shift group is required")
    @Column(name = "shift_group", nullable = false, length = 50)
    private String shiftGroup;

    /**
     * Date employee was hired
     * Used for tenure calculations and reporting
     */
    @NotNull(message = "Hire date is required")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Current employment status
     * Supports soft-delete functionality
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * Timestamp when record was created
     * Automatically set on insert
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when record was last updated
     * Automatically updated on modification
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User who created the record
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * User who last updated the record
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Automatically set timestamps before persist
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Employee role enumeration for RBAC
     * 
     * ADMIN - Full system access
     * HR - Employee management and reporting
     * SUPERVISOR - Team management and approvals
     * WORKER - Basic employee access
     */
    public enum Role {
        ADMIN,
        HR,
        SUPERVISOR,
        WORKER
    }

    /**
     * Employee status enumeration
     * 
     * ACTIVE - Currently employed and active
     * INACTIVE - Temporarily inactive (leave, suspension)
     * TERMINATED - Employment ended (soft delete)
     */
    public enum Status {
        ACTIVE,
        INACTIVE,
        TERMINATED
    }
}