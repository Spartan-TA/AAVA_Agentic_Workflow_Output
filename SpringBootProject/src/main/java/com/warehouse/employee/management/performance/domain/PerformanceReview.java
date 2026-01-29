package com.warehouse.employee.management.performance.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.warehouse.employee.management.employee.domain.Employee;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private LocalDate reviewDate;

    private Integer score;
    private String comments;
}
