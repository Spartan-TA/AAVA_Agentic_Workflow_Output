package com.company.wms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a performance review for an employee.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false, length = 100)
    private String reviewer;

    @Column(nullable = false, length = 255)
    private String summary;

    @Column(length = 255)
    private String goals;
}
