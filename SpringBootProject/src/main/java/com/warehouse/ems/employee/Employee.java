package com.warehouse.ems.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(name = "role", nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false; // Soft-delete flag
}
