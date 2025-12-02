package com.warehouse.ems.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees")
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Column(nullable = false)
    private String department;

    @NotBlank
    @Column(nullable = false)
    private String shiftGroup;

    @PastOrPresent
    @NotNull
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(nullable = false)
    private String status;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private String updatedBy;
    private OffsetDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    // Getters and setters omitted for brevity
}
