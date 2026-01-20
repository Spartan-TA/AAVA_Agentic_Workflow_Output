package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * PerformanceReview JPA entity.
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull
    private LocalDate reviewDate;

    private String reviewer;

    private String comments;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PerformanceReviewStatus status;

    public enum PerformanceReviewStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
