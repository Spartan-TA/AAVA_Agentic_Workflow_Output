package com.company.warehouse.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String badgeId;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String role;

    @Column(nullable = false)
    @NotBlank
    private String department;

    @Column(nullable = false)
    @NotBlank
    private String shiftGroup;

    @Column(nullable = false)
    @NotNull
    private LocalDate hireDate;

    @Column(nullable = false)
    @NotBlank
    private String status;
}
