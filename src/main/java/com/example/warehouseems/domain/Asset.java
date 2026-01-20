package com.example.warehouseems.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Asset JPA entity.
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
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    private String serialNumber;

    public enum AssetType {
        VEHICLE, EQUIPMENT, TOOL, OTHER
    }

    public enum AssetStatus {
        AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED
    }
}
