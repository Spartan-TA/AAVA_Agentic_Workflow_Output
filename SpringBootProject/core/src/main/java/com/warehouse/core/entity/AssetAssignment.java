package com.warehouse.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    @Column(name = "asset_type", nullable = false)
    private String assetType; // Scanner, Forklift, PPE

    @NotBlank
    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "condition_state")
    private String conditionState;

    @Column(name = "overdue")
    private Boolean overdue = false;
}
