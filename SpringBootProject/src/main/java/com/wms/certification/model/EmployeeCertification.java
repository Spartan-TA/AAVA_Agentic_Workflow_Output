package com.wms.certification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing an employee's certification record
 */
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

    /**
     * Employee ID
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * Certification reference
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;

    /**
     * Date certification was obtained
     */
    @Column(nullable = false)
    private LocalDate obtainedDate;

    /**
     * Expiry date of certification
     */
    private LocalDate expiryDate;

    /**
     * Is this certification active for the employee?
     */
    @Column(nullable = false)
    private boolean active;
}
