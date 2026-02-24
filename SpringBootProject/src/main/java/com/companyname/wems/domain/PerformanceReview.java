package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * PerformanceReview entity for tracking employee performance reviews.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Column(nullable = false)
    private Long templateId;

    @NotBlank
    @Size(max = 50)
    private String cycle;

    @NotBlank
    @Size(max = 20)
    private String status; // DRAFT, SUBMITTED, APPROVED

    private LocalDateTime submittedAt;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
