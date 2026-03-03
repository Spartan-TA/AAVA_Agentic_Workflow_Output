package com.wms.ems.performance.model;

import com.wms.ems.common.BaseEntity;
import com.wms.ems.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity representing a performance review for an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview extends BaseEntity {

    /**
     * The employee being reviewed.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Review cycle (e.g., Q1 2024).
     */
    private String cycle;

    /**
     * Goals for the review period.
     */
    @Column(length = 2000)
    private String goals;

    /**
     * Competencies for the review period.
     */
    @Column(length = 2000)
    private String competencies;

    /**
     * Numeric rating for the review.
     */
    private int rating;

    /**
     * Comments for the review.
     */
    @Column(length = 2000)
    private String comments;

    /**
     * Whether the review was acknowledged by the supervisor.
     */
    @Builder.Default
    private boolean acknowledgedBySupervisor = false;

    /**
     * Whether the review was acknowledged by the employee.
     */
    @Builder.Default
    private boolean acknowledgedByEmployee = false;
}