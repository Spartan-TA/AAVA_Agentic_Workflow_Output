package com.company.wms.employee;

import com.company.wms.employee.EmployeeRole;
import com.company.wms.employee.EmployeeStatus;
import com.company.wms.employee.Department;
import com.company.wms.employee.ShiftGroup;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmployeeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    private LocalDate hireDate;
    private LocalDate terminationDate;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and setters omitted for brevity
    // Add logging and error handling in service layer
}
