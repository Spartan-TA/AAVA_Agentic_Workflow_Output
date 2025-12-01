package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, etc.

    @Column(nullable = false)
    private boolean deleted = false; // Soft delete

    // Relationships (examples, can be expanded as needed)
    // @OneToMany(mappedBy = "employee")
    // private Set<AttendanceEvent> attendanceEvents;
    // ...
}
