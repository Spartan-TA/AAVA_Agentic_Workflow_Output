package com.example.warehouse.employee;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Employee entity representing warehouse staff.
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

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;

    @Column(nullable = false)
    private boolean deleted = false; // Soft delete flag
}
