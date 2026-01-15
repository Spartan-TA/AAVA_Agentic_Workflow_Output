package com.warehouse.ems.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_badge_id", columnList = "badge_id", unique = true),
    @Index(name = "idx_employee_department", columnList = "department")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "employee")
    private Set<com.warehouse.ems.attendance.entity.AttendanceEvent> attendanceEvents;
}