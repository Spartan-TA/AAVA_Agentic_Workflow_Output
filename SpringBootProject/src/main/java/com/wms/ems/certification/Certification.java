package com.wms.ems.certification;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String proofDocumentUrl;

    // Getters and setters omitted for brevity
}
