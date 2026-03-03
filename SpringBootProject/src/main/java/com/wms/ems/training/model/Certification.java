package com.wms.ems.training.model;

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
 * Entity representing a certification held by an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certifications")
public class Certification extends BaseEntity {

    /**
     * The employee who holds the certification.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Type of certification.
     */
    @Column(nullable = false)
    private String type;

    /**
     * Date the certification was issued.
     */
    private LocalDate issueDate;

    /**
     * Expiry date of the certification.
     */
    private LocalDate expiryDate;

    /**
     * URL to the certification document.
     */
    private String documentUrl;
}