package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Certification entity for employee certifications.
 */
@Entity
@Table(name = "certifications", indexes = {
        @Index(name = "idx_certification_employee", columnList = "employee_id"),
        @Index(name = "idx_certification_status", columnList = "status"),
        @Index(name = "idx_certification_expiry", columnList = "expiryDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee holding this certification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @NotBlank
    @Size(max = 150)
    private String certificationName;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 150)
    private String issuingAuthority;

    @Size(max = 255)
    private String documentUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    private Boolean alertSent;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Certification status.
     */
    public enum Status {
        ACTIVE, EXPIRED, REVOKED
    }
}
