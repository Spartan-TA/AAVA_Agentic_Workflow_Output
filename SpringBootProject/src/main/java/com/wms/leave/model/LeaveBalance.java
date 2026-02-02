package com.wms.leave.model;

import com.wms.leave.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an employee's leave balance for a specific leave type
 */
@Entity
@Table(name = "leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee ID
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * Type of leave
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    /**
     * Number of days available
     */
    @Column(nullable = false)
    private Double balance;
}
