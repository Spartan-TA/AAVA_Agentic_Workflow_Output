package com.warehouse.ems.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters omitted for brevity
    // Add constructors, equals, hashCode, toString as needed
}
