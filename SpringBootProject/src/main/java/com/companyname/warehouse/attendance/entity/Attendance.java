package com.companyname.warehouse.attendance.entity;

import com.companyname.warehouse.common.entity.BaseEntity;
import com.companyname.warehouse.employee.entity.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Attendance entity for clock-in/clock-out records.
 */
@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private String location;
    private String notes;
}
