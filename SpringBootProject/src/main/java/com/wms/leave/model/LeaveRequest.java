package com.wms.leave.model;

import com.wms.leave.enums.LeaveStatus;
import com.wms.leave.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a leave request by an employee
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

    /**
     * Employee ID requesting leave
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * Type of leave (e.g., SICK, VACATION)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    /**
     * Start date of leave
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * End date of leave
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Status of the leave request
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    /**
     * Reason for leave
     */
    private String reason;

    /**
     * Approver's employee ID
     */
    private Long approverId;
}
