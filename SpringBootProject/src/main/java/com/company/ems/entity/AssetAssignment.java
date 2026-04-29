package com.company.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * AssetAssignment entity for asset check-in/out.
 */
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @NotNull
    @Column(name = "checkout_time", nullable = false)
    private LocalDateTime checkoutTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "condition_on_return")
    private String conditionOnReturn;

    // Getters and setters omitted for brevity
}
