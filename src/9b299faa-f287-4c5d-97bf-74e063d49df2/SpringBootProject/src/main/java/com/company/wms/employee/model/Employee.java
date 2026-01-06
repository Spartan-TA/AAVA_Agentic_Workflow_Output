package com.company.wms.employee.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Entity representing a warehouse employee.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Employee full name */
    @Column(nullable = false)
    private String name;

    /** Unique badge identifier */
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    /** Employee role (ADMIN, HR, SUPERVISOR, WORKER) */
    @Column(nullable = false)
    private String role;

    /** Department name */
    @Column(nullable = false)
    private String department;

    /** Shift group */
    @Column(name = "shift_group")
    private String shiftGroup;

    /** Hire date */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /** Employment status (ACTIVE, INACTIVE, TERMINATED, etc.) */
    @Column(nullable = false)
    private String status;

    /** Soft delete flag */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Relationships to other entities (e.g., certifications, assignments) will be added as needed.
}
