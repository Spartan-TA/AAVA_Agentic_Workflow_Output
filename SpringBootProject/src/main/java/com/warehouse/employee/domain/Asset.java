package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Asset entity for equipment and assets.
 */
@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_asset_status", columnList = "status"),
        @Index(name = "idx_asset_asset_type", columnList = "assetType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Size(max = 255)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Condition condition;

    /**
     * The employee to whom this asset is assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @JsonIgnore
    private Employee assignedTo;

    private LocalDateTime assignedAt;

    private LocalDateTime dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Asset types.
     */
    public enum AssetType {
        SCANNER, FORKLIFT, PPE, COMPUTER
    }

    /**
     * Asset condition.
     */
    public enum Condition {
        GOOD, FAIR, POOR, DAMAGED
    }

    /**
     * Asset status.
     */
    public enum Status {
        AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED
    }
}
