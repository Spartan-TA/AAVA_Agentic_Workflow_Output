package com.warehouse.ems.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employee master data.
 * Supports CRUD operations, soft delete, and role-based access control.
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employees", 
       uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"),
       indexes = {
           @Index(name = "idx_employee_badge", columnList = "badge_id"),
           @Index(name = "idx_employee_department", columnList = "department"),
           @Index(name = "idx_employee_status", columnList = "status")
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
     * Used for clock-in/out and access control.
     */
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    private String badgeId;
    
    /**
     * Full name of the employee.
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Role of the employee (ADMIN, HR, SUPERVISOR, WORKER).
     * Used for RBAC and access control.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
    
    /**
     * Department where the employee works.
     */
    @Column(nullable = false, length = 50)
    private String department;
    
    /**
     * Shift group assignment (e.g., "Morning", "Evening", "Night").
     */
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    /**
     * Date when the employee was hired.
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    /**
     * Current employment status.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    /**
     * Soft delete flag. When true, employee is logically deleted.
     */
    @Column(nullable = false)
    private boolean deleted = false;
    
    /**
     * Tenant ID for multi-tenant support.
     */
    @Column(name = "tenant_id")
    private Long tenantId;
    
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