package com.company.wms.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employee details.
 */
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotBlank
    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters omitted for brevity
    // ...
}
