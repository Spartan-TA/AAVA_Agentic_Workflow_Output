package com.wms.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "soft_deleted", nullable = false)
    private boolean softDeleted = false;

    // Relationships
    @OneToMany(mappedBy = "employee")
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "employee")
    private Set<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private Set<AssetAssignment> assetAssignments;
}