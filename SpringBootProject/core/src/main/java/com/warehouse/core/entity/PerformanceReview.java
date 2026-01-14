package com.warehouse.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    @Column(name = "cycle", nullable = false)
    private String cycle; // Quarterly, Annual

    @NotNull
    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "goals")
    private String goals;

    @Column(name = "competencies")
    private String competencies;

    @Column(name = "ratings")
    private String ratings;

    @Column(name = "comments")
    private String comments;

    @Column(name = "supervisor_acknowledged")
    private Boolean supervisorAcknowledged = false;

    @Column(name = "employee_acknowledged")
    private Boolean employeeAcknowledged = false;

    @Column(name = "pdf_url")
    private String pdfUrl;
}
