package com.warehouse.employee.management.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee JPA entity representing warehouse staff.
 * Supports soft-delete and unique badgeId enforcement.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 32)
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "role", nullable = false)
    private String role;

    @Size(max = 64)
    @Column(name = "department")
    private String department;

    @Size(max = 32)
    @Column(name = "shift_group")
    private String shiftGroup;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 16)
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Relationships (example: certifications, attendance, etc.)
    // @OneToMany(mappedBy = "employee")
    // private Set<Attendance> attendances;

    // Getters and setters omitted for brevity
}
