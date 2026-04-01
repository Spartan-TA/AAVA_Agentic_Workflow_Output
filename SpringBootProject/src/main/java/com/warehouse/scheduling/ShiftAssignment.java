package com.warehouse.scheduling;

import com.warehouse.employee.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ShiftAssignment entity for assigning employees to shifts.
 */
@Entity
@Table(name = "shift_assignment")
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_schedule_id")
    private ShiftSchedule shiftSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "status", nullable = false)
    private String status;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters and setters omitted for brevity
}
