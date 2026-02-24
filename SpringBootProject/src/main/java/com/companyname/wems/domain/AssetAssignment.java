package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

/**
 * AssetAssignment entity for tracking asset assignments to employees.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long assetId;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotNull
    private LocalDateTime checkoutTime;

    private LocalDateTime returnTime;

    @Size(max = 100)
    private String conditionOnReturn;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}
