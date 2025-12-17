package com.wms.employee;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing an employee certification (e.g., forklift).
 */
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String certificationType;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String proofDocumentUrl;

    // Getters and setters omitted for brevity
    // ...
}
