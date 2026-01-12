package com.warehouseems.review;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private com.warehouseems.employee.Employee employee;

    @Column(nullable = false)
    private LocalDate reviewDate;

    private String template;
    private String goals;
    private Integer rating;
    private String comments;
    private Boolean acknowledgedEmployee;
    private Boolean acknowledgedSupervisor;
}
