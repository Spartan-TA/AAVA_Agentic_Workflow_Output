package com.company.wms.leave.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a leave request by an employee.
 */
@Entity
@Table(name = "leave_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String leaveType; // e.g., SICK, VACATION, UNPAID

    @Column(nullable = false)
    private String status; // e.g., PENDING, APPROVED, REJECTED

    @Column
    private String remarks;
}
