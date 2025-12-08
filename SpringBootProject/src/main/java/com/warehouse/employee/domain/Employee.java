package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    // Additional relationships (leave, attendance, etc.) can be added here
}

/**
 * Employee status enumeration.
 */
enum EmployeeStatus {
    ACTIVE,
    INACTIVE,
    TERMINATED,
    ON_LEAVE
}
