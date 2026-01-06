package com.warehouse.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

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

    @Column(name = "badge_id", nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(length = 50)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    // Relationships to other entities (e.g., certifications, attendance, assets) can be added here
    // Example:
    // @OneToMany(mappedBy = "employee")
    // private Set<Attendance> attendances;
}
