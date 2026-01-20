package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Certification JPA entity.
 */
@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CertificationStatus status;

    public enum CertificationStatus {
        ACTIVE, EXPIRED, REVOKED, PENDING
    }
}
