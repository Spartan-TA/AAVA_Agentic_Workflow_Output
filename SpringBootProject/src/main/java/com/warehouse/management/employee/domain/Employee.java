package com.warehouse.management.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotNull
    @Column(name = "role", nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotNull
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
