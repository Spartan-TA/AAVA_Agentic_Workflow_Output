package com.company.wms.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employee master data.
 * Supports soft delete pattern and multi-tenancy.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_badge_id", columnList = "badge_id"),
    @Index(name = "idx_department", columnList = "department"),
    @Index(name = "idx_deleted", columnList = "deleted")
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
     * Unique badge ID for employee identification
     */
    @Column(unique = true, nullable = false, length = 32, name = "badge_id")
    private String badgeId;

    /**
     * Full name of the employee
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Employee role (ADMIN, HR, SUPERVISOR, WORKER)
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Department assignment (e.g., Shipping, Receiving, Inventory)
     */
    @Column(length = 50)
    private String department;

    /**
     * Shift group assignment for scheduling
     */
    @Column(length = 50, name = "shift_group")
    private String shiftGroup;

    /**
     * Date employee was hired
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * Current employment status (ACTIVE, INACTIVE, ON_LEAVE, TERMINATED)
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Soft delete flag - true if employee record is deleted
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    /**
     * Tenant ID for multi-tenancy support
     */
    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    /**
     * Email address for notifications
     */
    @Column(length = 100)
    private String email;

    /**
     * Phone number for SMS notifications
     */
    @Column(length = 20)
    private String phone;

    /**
     * Pre-remove hook to set deleted flag instead of actual deletion
     */
    @PreRemove
    public void preRemove() {
        this.deleted = true;
    }

    /**
     * Check if employee is active
     * @return true if status is ACTIVE and not deleted
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && !deleted;
    }
}