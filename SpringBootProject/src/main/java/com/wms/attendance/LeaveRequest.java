package com.wms.attendance;

import javax.persistence.*;
import java.time.LocalDate;

import com.wms.employee.Employee;

/**
 * Entity representing a leave request (PTO, sick, unpaid).
 */
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String leaveType; // PTO, SICK, UNPAID

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status; // REQUESTED, APPROVED, DENIED

    private String approver;
    private String comments;

    // Getters and setters omitted for brevity
    // ...
}
