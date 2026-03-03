package com.wms.ems.attendance.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an attendance event for an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent extends BaseEntity {

    /**
     * The employee associated with this attendance event.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Clock-in time.
     */
    private LocalDateTime clockIn;

    /**
     * Clock-out time.
     */
    private LocalDateTime clockOut;

    /**
     * Device ID used for clocking in/out.
     */
    private String deviceId;

    /**
     * Location of the attendance event.
     */
    private String location;

    /**
     * Whether a correction was requested for this event.
     */
    @Builder.Default
    private boolean correctionRequested = false;

    /**
     * Number of hours worked in this event.
     */
    private Double hoursWorked;
}
