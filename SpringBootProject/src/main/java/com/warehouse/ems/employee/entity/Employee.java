package com.warehouse.ems.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
 * This entity stores all employee master data including personal information,
 * role assignments, department, shift group, and employment status.
 * 
 * Supports soft-delete functionality to preserve historical data.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employee", uniqueConstraints = {
    @UniqueConstraint(columnNames = "badge_id")
})
@Data
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
     * Full name of the employee.
     */
    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Unique badge ID for the employee.
     * Used for clock-in/out and access control.
     */
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;

    /**
     * Role of the employee (ADMIN, HR, SUPERVISOR, WORKER).
     * Used for RBAC authorization.
     */
    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must not exceed 50 characters")
    @Column(nullable = false)
    private String role;

    /**
     * Department where the employee works.
     */
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    /**
     * Shift group assignment for scheduling.
     */
    @Size(max = 100, message = "Shift group must not exceed 100 characters")
    @Column(name = "shift_group")
    private String shiftGroup;

    /**
     * Date when the employee was hired.
     */
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date must be in the past or present")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Current employment status (ACTIVE, INACTIVE, TERMINATED).
     */
    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    @Column(nullable = false)
    private String status;

    /**
     * Soft-delete flag. When true, the employee is logically deleted
     * but data is preserved for historical purposes.
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    /**
     * Timestamp when the employee record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when the employee record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * Pre-persist callback to set creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update callback to update modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}