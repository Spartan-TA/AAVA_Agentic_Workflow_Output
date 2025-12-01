package com.wms.ems.performance.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.wms.ems.employee.entity.Employee;

/**
 * PerformanceReview entity representing employee performance reviews.
 */
@Entity
@Table(name = "performance_review")
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
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}