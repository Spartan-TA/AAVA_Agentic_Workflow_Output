package com.warehouse.employee.domain;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String firstName;

    @NotNull
    @Size(min = 2, max = 100)
    private String lastName;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    @Size(min = 10, max = 15)
    private String phoneNumber;

    @NotNull
    private Long tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    private String createdBy;

    @NotNull
    private String updatedBy;

    @Column(nullable = false)
    private Boolean deleted = false;

    // Getters and Setters
    // ...

    public Employee() {}

    public Employee(String firstName, String lastName, String email, String phoneNumber, Long tenantId, String createdBy, String updatedBy) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.tenantId = tenantId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.deleted = false;
    }

    // Getters and setters omitted for brevity
}
