package com.warehouse.performance.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

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
    private Long employeeId;

    @NotNull
    private LocalDate reviewDate;

    @NotBlank
    private String reviewer;

    @NotBlank
    private String comments;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Rating rating;

    public enum Rating {
        EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT, UNSATISFACTORY
    }
}
