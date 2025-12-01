package com.warehouse.ems.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false, length = 64)
    private String department;

    @Column(length = 32)
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, length = 16)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(nullable = false)
    private Boolean deleted = false; // Soft delete flag
}
