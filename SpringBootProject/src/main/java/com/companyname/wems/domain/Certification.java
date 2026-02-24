package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * Certification entity for tracking available certifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
