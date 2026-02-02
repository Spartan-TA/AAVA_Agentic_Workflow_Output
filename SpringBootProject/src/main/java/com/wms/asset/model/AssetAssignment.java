package com.wms.asset.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing the assignment of an asset to an employee
 */
@Entity
@Table(name = "asset_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee ID to whom the asset is assigned
     */
    @Column(nullable = false)
    private Long employeeId;

    /**
     * Asset reference
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    /**
     * Assignment start date
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Assignment end date (nullable)
     */
    private LocalDate endDate;

    /**
     * Is this assignment active?
     */
    @Column(nullable = false)
    private boolean active;
}
