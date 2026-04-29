package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * LeaveRequest entity for PTO, sick, unpaid leave.
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

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status")
    private String status;

    @Column(name = "accrual_balance")
    private Double accrualBalance;

    // Getters and setters omitted for brevity
}
