package com.companyname.warehouse.scheduling.entity;

import com.companyname.warehouse.common.entity.BaseEntity;
import com.companyname.warehouse.employee.entity.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Shift entity for scheduling employee shifts.
 */
@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location;
    private String shiftType;
    private String notes;
}
