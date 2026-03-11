package com.warehouse.ems.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // Scanner, Forklift, PPE, etc.

    @Column(name = "condition_state")
    private String conditionState;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssetAssignment> assignments;

    // Getters and setters
    // ... (omitted for brevity)
}
