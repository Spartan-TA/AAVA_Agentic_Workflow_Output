package com.warehouse.ems.employee;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true, length = 50)
    private String badgeId;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(length = 50)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}