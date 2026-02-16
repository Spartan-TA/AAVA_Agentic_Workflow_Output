package com.warehouse.employeemgmt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * LeaveRequest Entity - Leave and absence management
 * 
 * Handles PTO, sick, and unpaid leave requests with approval workflows.
 * Integrates with scheduling and payroll systems.
 * 
 * Features:
 * - Multiple leave types (PTO, SICK, UNPAID)
 * - Approval workflow (PENDING, APPROVED, DENIED)
 * - Balance tracking and accrual
 * - Integration with scheduling for coverage
 * - Payroll export support
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 20)
    private String type; // PTO, SICK, UNPAID

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, DENIED

    @Column(length = 500)
    private String reason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "denial_reason", length = 500)
    private String denialReason;

    @Column(name = "days_requested")
    private Integer daysRequested;

    /**
     * Calculate number of days requested
     */
    public void calculateDays() {
        if (startDate != null && endDate != null) {
            this.daysRequested = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
    }
}