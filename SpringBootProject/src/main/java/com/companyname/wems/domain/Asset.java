package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * Asset entity for tracking assets in the warehouse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String assetTag;

    @NotBlank
    @Size(max = 20)
    private String status; // AVAILABLE, ASSIGNED, MAINTENANCE

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
