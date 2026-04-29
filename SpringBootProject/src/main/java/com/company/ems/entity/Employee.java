package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee entity representing warehouse staff.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(name = "role", nullable = false)
    private String role;

    @NotBlank
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @PastOrPresent
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "employee")
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "employee")
    private Set<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee")
    private Set<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private Set<SafetyIncident> safetyIncidents;

    @OneToMany(mappedBy = "employee")
    private Set<AssetAssignment> assetAssignments;

    // Getters and setters omitted for brevity
}
