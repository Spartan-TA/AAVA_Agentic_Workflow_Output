package com.warehouse.employee_mgmt.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    private String name;

    @NotBlank
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    private String department;

    @NotBlank
    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}