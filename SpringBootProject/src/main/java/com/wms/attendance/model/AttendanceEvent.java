package com.wms.attendance.model;

import com.wms.common.model.BaseEntity;
import com.wms.employee.model.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AttendanceEvent entity representing a clock-in or clock-out event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false, length = 10)
    private String eventType; // CLOCK_IN or CLOCK_OUT

    @Column(length = 255)
    private String remarks;
}
