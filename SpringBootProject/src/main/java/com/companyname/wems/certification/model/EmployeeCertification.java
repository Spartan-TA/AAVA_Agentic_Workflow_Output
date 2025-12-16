package com.companyname.wems.certification.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee_certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Column(nullable = false)
    private String certificationName;

    @NotNull
    @Column(nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotBlank
    @Column(nullable = false)
    private String status; // ACTIVE, EXPIRED, EXPIRING_SOON

    private String documentUrl;
}