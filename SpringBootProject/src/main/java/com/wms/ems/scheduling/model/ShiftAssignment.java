package com.wms.ems.scheduling.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a shift assignment for an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment extends BaseEntity {

    /**
     * The employee assigned to the shift.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * The shift template assigned.
     */
    @ManyToOne
    @JoinColumn(name = "shift_template_id")
    private ShiftTemplate shiftTemplate;

    /**
     * The date of the shift assignment.
     */
    @Column(nullable = false)
    private LocalDate date;
}