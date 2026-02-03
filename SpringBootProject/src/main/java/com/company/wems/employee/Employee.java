package com.company.wems.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employees.
 * Supports soft-delete and unique badgeId enforcement.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
@Getter
@Setter
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

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
