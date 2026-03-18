package com.company.wms.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an asset assigned to an employee.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 100)
    private String assetTag;

    @Column(nullable = false, length = 100)
    private String assetType;

    @Column(length = 255)
    private String description;
}
