package com.wms.scheduling;

import javax.persistence.*;
import java.time.LocalDate;

import com.wms.employee.Employee;

/**
 * Entity representing an assignment of a shift to an employee.
 */
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private LocalDate shiftDate;

    private Boolean overtime;
    private Boolean blackout;

    // Getters and setters omitted for brevity
    // ...
}
