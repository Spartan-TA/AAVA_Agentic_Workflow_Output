package com.warehouse.employee.management.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee JPA entity representing warehouse employees.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 32)
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
