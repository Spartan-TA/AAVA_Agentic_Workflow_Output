package com.warehouse.employee.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employee master data.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Getters and setters omitted for brevity
    // ...

    // Constructors, equals, hashCode, toString
    // ...
}
