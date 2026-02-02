package com.wms.performance.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a performance review for an employee
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

    /**
     * Employee ID being reviewed
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * Reviewer employee ID
     */
    @Column(nullable = false)
    private Long reviewerId;

    /**
     * Review period start date
     */
    @Column(nullable = false)
    private LocalDate periodStart;

    /**
     * Review period end date
     */
    @Column(nullable = false)
    private LocalDate periodEnd;

    /**
     * Overall rating (e.g., 1-5)
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Comments
     */
    @Column(length = 2000)
    private String comments;

    /**
     * Goals set for next period
     */
    @Column(length = 2000)
    private String goals;
}
