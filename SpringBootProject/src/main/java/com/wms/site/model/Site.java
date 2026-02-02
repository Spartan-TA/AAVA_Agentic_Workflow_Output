package com.wms.site.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a physical warehouse site.
 */
@Data
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Site name */
    @Column(nullable = false)
    private String name;

    /** Time zone of the site (e.g., America/New_York) */
    @Column(nullable = false)
    private String timeZone;

    /** Locale (e.g., en_US, es_ES, fr_FR) */
    @Column(nullable = false)
    private String locale;

    /** Physical address */
    @Column(nullable = false)
    private String address;
}
