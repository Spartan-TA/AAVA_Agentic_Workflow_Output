package com.warehouse.ems.employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing an employee in the warehouse EMS.
 *
 * @author Spartan-TA
 * @since 1.0.0
 */
@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"badge_id"}),
        @UniqueConstraint(columnNames = {"email"})
})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Getters and setters omitted for brevity
    // ...
}
