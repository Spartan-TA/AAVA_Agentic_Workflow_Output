package com.companyname.wems.employee.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Employee entity for Employee Master Data CRUD (E02)
 * Includes validation, soft-delete, and audit fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 20)
    @Column(name = "phone")
    private String phone;

    @NotNull
    @Size(max = 50)
    @Column(name = "department", nullable = false)
    private String department;

    @NotNull
    @Size(max = 50)
    @Column(name = "position", nullable = false)
    private String position;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /**
     * Employee status: ACTIVE, INACTIVE, DELETED (soft-delete)
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
