package com.warehouse.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a warehouse employee.
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

    @Column(nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String department;

    private String shiftGroup;

    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(nullable = false)
    private Boolean deleted = false; // Soft delete flag
}
