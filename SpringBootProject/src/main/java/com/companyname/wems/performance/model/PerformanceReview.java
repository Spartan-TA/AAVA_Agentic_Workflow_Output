package com.companyname.wems.performance.model;

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
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Column(nullable = false)
    private String reviewCycle;

    @NotNull
    @Column(nullable = false)
    private LocalDate reviewDate;

    @NotNull
    @Column(nullable = false)
    private Double rating;

    private String goals;
    private String competencies;
    private String comments;
    private Long reviewerId;
}