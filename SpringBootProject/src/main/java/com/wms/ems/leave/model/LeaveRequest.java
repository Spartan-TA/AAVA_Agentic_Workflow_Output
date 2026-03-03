package com.wms.ems.leave.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import com.wms.ems.common.LeaveType;
import com.wms.ems.common.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a leave request for an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {

    /**
     * The employee requesting leave.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Type of leave requested.
     */
    @Enumerated(EnumType.STRING)
    private LeaveType type;

    /**
     * Start date of the leave.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * End date of the leave.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Status of the leave request.
     */
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    /**
     * Reason for the leave request.
     */
    @Column(length = 500)
    private String reason;
}