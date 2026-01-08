package com.warehouseems.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(name = "role", nullable = false)
    private String role;

    @NotBlank
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Getters and setters omitted for brevity
    // Add constructors, equals, hashCode, toString as needed
}
