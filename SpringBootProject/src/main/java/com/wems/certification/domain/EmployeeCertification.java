package com.wems.certification.domain;

import com.wems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee_certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;

    @Enumerated(EnumType.STRING)
    private CertificationStatus status;

    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String notes;
}
