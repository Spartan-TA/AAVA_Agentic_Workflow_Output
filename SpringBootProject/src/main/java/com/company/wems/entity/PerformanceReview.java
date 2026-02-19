package com.company.wems.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Performance review entity for employee evaluations.
 */
@Entity
@Table(name = "performance_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "goals")
    private String goals;

    @Column(name = "comments")
    private String comments;

    @Column(name = "score")
    private Integer score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
