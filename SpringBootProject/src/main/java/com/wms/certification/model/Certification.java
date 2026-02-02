package com.wms.certification.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a certification type (e.g., Forklift License)
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

    /**
     * Name of the certification
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Description of the certification
     */
    private String description;

    /**
     * Is this certification active?
     */
    @Column(nullable = false)
    private boolean active;
}
