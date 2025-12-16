package com.warehouse.employee.management.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
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

    @NotBlank
    @Column(name = "shift_group", nullable = false)
    private String shiftGroup;

    @PastOrPresent
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
