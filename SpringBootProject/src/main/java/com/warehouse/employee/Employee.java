package com.warehouse.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employee master data.
 * This entity stores core employee information including badge ID, role, department,
 * shift assignments, and employment status.
 * 
 * Implements soft-delete pattern to maintain historical records.
 * 
 * @author Warehouse Development Team
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
public class Employee {
    
    /**
     * Unique identifier for the employee record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique badge ID for the employee. Used for physical access and time tracking.
     * Must be unique across all employees.
     */
    @NotBlank(message = "Badge ID is required")
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    private String badgeId;

    /**
     * Full name of the employee.
     */
    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Employee role (e.g., ADMIN, HR, SUPERVISOR, WORKER).
     */
    @NotBlank(message = "Role is required")
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Department assignment (e.g., Receiving, Shipping, Inventory).
     */
    @NotBlank(message = "Department is required")
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * Shift group assignment for scheduling purposes.
     */
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    /**
     * Date the employee was hired.
     */
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * Current employment status (e.g., ACTIVE, INACTIVE, TERMINATED).
     */
    @NotNull(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Soft-delete flag. When true, the employee record is logically deleted
     * but retained for historical and audit purposes.
     */
    @Column(nullable = false)
    private boolean deleted = false;

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
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * User who last updated the record.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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

    // Constructors
    public Employee() {}

    public Employee(String badgeId, String name, String role, String department) {
        this.badgeId = badgeId;
        this.name = name;
        this.role = role;
        this.department = department;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getShiftGroup() {
        return shiftGroup;
    }

    public void setShiftGroup(String shiftGroup) {
        this.shiftGroup = shiftGroup;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", badgeId='" + badgeId + ''' +
                ", name='" + name + ''' +
                ", role='" + role + ''' +
                ", department='" + department + ''' +
                ", status='" + status + ''' +
                ", deleted=" + deleted +
                '}';
    }
}