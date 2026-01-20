package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee JPA entity.
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private LocalDate hireDate;

    private LocalDate terminationDate;

    public enum EmployeeRole {
        ADMIN, MANAGER, SUPERVISOR, WORKER
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, TERMINATED, ON_LEAVE
    }
}
