package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PerformanceReview entity for employee performance reviews.
 */
@Entity
@Table(name = "performance_reviews", indexes = {
        @Index(name = "idx_performance_review_employee", columnList = "employee_id"),
        @Index(name = "idx_performance_review_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee being reviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @NotBlank
    @Size(max = 50)
    private String reviewPeriod;

    @NotNull
    private LocalDate reviewDate;

    /**
     * The reviewer (employee).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnore
    private Employee reviewer;

    @Min(1)
    @Max(5)
    private Integer overallRating;

    @Size(max = 1000)
    private String goals;

    @Size(max = 1000)
    private String competencies;

    @Size(max = 1000)
    private String strengths;

    @Size(max = 1000)
    private String areasForImprovement;

    @Size(max = 1000)
    private String comments;

    private Boolean employeeAcknowledged;

    @Size(max = 100)
    private String reviewerSignature;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Review status.
     */
    public enum Status {
        DRAFT, SUBMITTED, ACKNOWLEDGED
    }
}
