package com.warehouse.ems.performance;

import com.warehouse.ems.employee.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing an employee performance review.
 */
@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull
    private Employee employee;

    @NotBlank
    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @Column(name = "goals")
    private String goals;

    @Column(name = "competencies")
    private String competencies;

    @Column(name = "rating")
    private String rating;

    @Column(name = "comments")
    private String comments;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status;
}
