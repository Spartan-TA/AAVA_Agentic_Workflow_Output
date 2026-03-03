package com.wms.ems.employee.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.common.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing an Employee in the Warehouse EMS system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee extends BaseEntity {

    /**
     * Unique badge identifier for the employee.
     */
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    /**
     * Name of the employee.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Role of the employee.
     */
    private String role;

    /**
     * Department of the employee.
     */
    private String department;

    /**
     * Shift group of the employee.
     */
    private String shiftGroup;

    /**
     * Hire date of the employee.
     */
    private LocalDate hireDate;

    /**
     * Status of the employee.
     */
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    /**
     * Soft delete flag.
     */
    @Builder.Default
    private boolean deleted = false;
}
