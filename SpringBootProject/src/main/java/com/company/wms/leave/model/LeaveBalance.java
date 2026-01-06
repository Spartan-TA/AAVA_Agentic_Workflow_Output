package com.company.wms.leave.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing the leave balance for an employee.
 */
@Entity
@Table(name = "leave_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String leaveType; // e.g., SICK, VACATION

    @Column(nullable = false)
    private Double balance;
}
