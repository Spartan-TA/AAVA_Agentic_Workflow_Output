package com.warehouse.employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Asset entity for equipment and asset registry.
 */
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_tag", nullable = false, unique = true)
    private String assetTag;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "condition")
    private String condition;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssetAssignment> assetAssignments;

    // Getters and setters omitted for brevity
}
