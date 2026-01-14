package com.warehouse.ems.domain;

import com.warehouse.ems.enums.AssetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing an asset (equipment, PPE, etc.).
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
    @Column(unique = true, nullable = false)
    private String assetTag;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetType type;

    private String condition;
}
