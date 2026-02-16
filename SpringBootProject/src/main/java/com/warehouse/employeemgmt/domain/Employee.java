package com.warehouse.employeemgmt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee Entity - Core domain model for warehouse employees
 * 
 * Represents an employee in the warehouse management system with all necessary
 * attributes for CRUD operations, RBAC, attendance tracking, and compliance.
 * 
 * Features:
 * - Unique badge ID for identification
 * - Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
 * - Soft delete support for data retention
 * - Department and shift group assignments
 * - Hire date tracking for tenure calculations
 * - Status management (ACTIVE, INACTIVE, TERMINATED)
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true, length = 50)
    private String badgeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    // Relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Attendance> attendances;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Certification> certifications;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LeaveRequest> leaveRequests;
}