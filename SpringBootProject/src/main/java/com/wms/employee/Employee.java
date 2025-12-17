package com.wms.employee;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Entity representing a warehouse employee.
 */
@Entity
@Table(name = "employees")
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

    // Relationships to other entities (example)
    @OneToMany(mappedBy = "employee")
    private Set<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private Set<ShiftAssignment> shiftAssignments;

    @OneToMany(mappedBy = "employee")
    private Set<AttendanceEvent> attendanceEvents;

    // Getters and setters omitted for brevity
    // ...
}
