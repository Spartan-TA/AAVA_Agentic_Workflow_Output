package com.wms.asset.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an equipment or asset
 */
@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Asset name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Asset type (e.g., Forklift, Scanner)
     */
    @Column(nullable = false)
    private String type;

    /**
     * Serial number
     */
    @Column(unique = true)
    private String serialNumber;

    /**
     * Is this asset active?
     */
    @Column(nullable = false)
    private boolean active;
}
