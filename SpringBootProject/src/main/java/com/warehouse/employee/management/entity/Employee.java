package com.warehouse.employee.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

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

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    @Column(nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @PastOrPresent
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @NotBlank
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted")
    private boolean deleted = false;
}