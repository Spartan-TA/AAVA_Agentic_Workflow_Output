package com.company.wem.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = {"badge_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    // Relationships (examples, to be expanded)
    // @OneToMany(mappedBy = "employee")
    // private Set<Attendance> attendances;
}
