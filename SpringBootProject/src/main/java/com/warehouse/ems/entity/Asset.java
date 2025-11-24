package com.warehouse.ems.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Asset entity for equipment and asset assignment.
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

    @NotBlank
    @Column(name = "asset_type", nullable = false)
    private String assetType; // e.g., SCANNER, FORKLIFT, PPE

    @NotBlank
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @NotBlank
    @Column(name = "condition", nullable = false)
    private String condition; // e.g., GOOD, NEEDS_REPAIR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    @Column(name = "checkout_date")
    private LocalDateTime checkoutDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
