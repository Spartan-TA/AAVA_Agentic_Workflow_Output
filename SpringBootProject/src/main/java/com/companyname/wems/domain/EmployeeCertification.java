package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * EmployeeCertification entity for tracking employee certifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_certifications")
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    @Column(nullable = false)
    private Long certificationId;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 255)
    private String documentUrl;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
