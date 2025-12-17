package com.warehouse.employee.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PerformanceReview entity for employee review cycles.
 * Includes audit fields, reviewer, rating, and comments.
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
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @NotNull
    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @NotNull
    @Column(nullable = false)
    private Integer rating;

    @Column(name = "comments")
    private String comments;

    @NotBlank
    @Column(nullable = false)
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
